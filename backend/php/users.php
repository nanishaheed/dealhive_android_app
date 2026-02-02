<?php
/**
 * Deal Hive - Users API
 * 
 * Endpoints:
 * POST /users.php?action=register - Register new user
 * POST /users.php?action=login - Login user
 * GET /users.php?id=1 - Get user profile
 * GET /users.php?all=true - Get all users (admin)
 * PUT /users.php?id=1 - Update user profile
 * DELETE /users.php?id=1 - Delete user
 */

require_once 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = $_GET['action'] ?? null;
$db = getDB();

switch ($method) {
    case 'GET':
        if (isset($_GET['all']) && $_GET['all'] === 'true') {
            // Get all users (for admin)
            $stmt = $db->prepare("
                SELECT id, name, email, phone, role, created_at 
                FROM users ORDER BY created_at DESC
            ");
            $stmt->execute();
            $users = $stmt->fetchAll();
            sendResponse(['users' => $users, 'count' => count($users)]);
        } elseif (!isset($_GET['id'])) {
            sendError("User ID required");
        } else {
            $stmt = $db->prepare("
                SELECT id, name, email, phone, avatar, role, created_at 
                FROM users WHERE id = ?
            ");
            $stmt->execute([$_GET['id']]);
            $user = $stmt->fetch();
            
            if ($user) {
                // Get user stats
                $statsStmt = $db->prepare("
                    SELECT 
                        (SELECT COUNT(*) FROM comparisons WHERE user_id = ?) as comparisons,
                        (SELECT COUNT(*) FROM saved_items WHERE user_id = ?) as saved_items
                ");
                $statsStmt->execute([$_GET['id'], $_GET['id']]);
                $stats = $statsStmt->fetch();
                
                $user['stats'] = [
                    'comparisons' => (int)$stats['comparisons'],
                    'saved_items' => (int)$stats['saved_items']
                ];
                
                sendResponse($user);
            } else {
                sendError("User not found", 404);
            }
        }
        break;
        
    case 'POST':
        $data = getInput();
        
        if ($action === 'register') {
            validateRequired($data, ['name', 'email', 'password']);
            
            // Check if email exists
            $checkStmt = $db->prepare("SELECT id FROM users WHERE email = ?");
            $checkStmt->execute([$data['email']]);
            if ($checkStmt->fetch()) {
                sendError("Email already registered");
            }
            
            // Hash password
            $hashedPassword = password_hash($data['password'], PASSWORD_DEFAULT);
            
            // Get role (default to 'user')
            $role = isset($data['role']) ? $data['role'] : 'user';
            
            $stmt = $db->prepare("
                INSERT INTO users (name, email, password, phone, role) 
                VALUES (?, ?, ?, ?, ?)
            ");
            $stmt->execute([
                $data['name'],
                $data['email'],
                $hashedPassword,
                $data['phone'] ?? null,
                $role
            ]);
            
            $userId = $db->lastInsertId();
            
            sendResponse([
                'message' => 'User registered successfully',
                'user' => [
                    'id' => $userId,
                    'name' => $data['name'],
                    'email' => $data['email'],
                    'role' => $role
                ]
            ], 201);
            
        } elseif ($action === 'login') {
            validateRequired($data, ['email', 'password']);
            
            $stmt = $db->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->execute([$data['email']]);
            $user = $stmt->fetch();
            
            if ($user && password_verify($data['password'], $user['password'])) {
                // Remove password from response
                unset($user['password']);
                
                sendResponse([
                    'message' => 'Login successful',
                    'user' => $user
                ]);
            } else {
                sendError("Invalid email or password", 401);
            }
            
        } else {
            sendError("Invalid action. Use ?action=register or ?action=login");
        }
        break;
        
    case 'PUT':
        if (!isset($_GET['id'])) {
            sendError("User ID required");
        }
        
        $data = getInput();
        $id = $_GET['id'];
        
        // Check if user exists
        $checkStmt = $db->prepare("SELECT id FROM users WHERE id = ?");
        $checkStmt->execute([$id]);
        if (!$checkStmt->fetch()) {
            sendError("User not found", 404);
        }
        
        $updates = [];
        $params = [];
        
        if (isset($data['name'])) {
            $updates[] = "name = ?";
            $params[] = $data['name'];
        }
        
        if (isset($data['phone'])) {
            $updates[] = "phone = ?";
            $params[] = $data['phone'];
        }
        
        if (isset($data['avatar'])) {
            $updates[] = "avatar = ?";
            $params[] = $data['avatar'];
        }
        
        if (isset($data['password'])) {
            $updates[] = "password = ?";
            $params[] = password_hash($data['password'], PASSWORD_DEFAULT);
        }
        
        if (isset($data['role'])) {
            $updates[] = "role = ?";
            $params[] = $data['role'];
        }
        
        if (empty($updates)) {
            sendError("No fields to update");
        }
        
        $params[] = $id;
        $stmt = $db->prepare("UPDATE users SET " . implode(', ', $updates) . " WHERE id = ?");
        $stmt->execute($params);
        
        sendResponse(['message' => 'User updated']);
        break;
        
    case 'DELETE':
        if (!isset($_GET['id'])) {
            sendError("User ID required");
        }
        
        $stmt = $db->prepare("DELETE FROM users WHERE id = ?");
        $stmt->execute([$_GET['id']]);
        
        if ($stmt->rowCount() > 0) {
            sendResponse(['message' => 'User deleted']);
        } else {
            sendError("User not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
