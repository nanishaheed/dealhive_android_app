<?php
/**
 * Deal Hive - Saved Items API
 * 
 * Endpoints:
 * GET /saved.php?user_id=1 - Get user's saved items
 * POST /saved.php - Save an item
 * DELETE /saved.php?user_id=1&product_id=1 - Remove saved item
 */

require_once 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

switch ($method) {
    case 'GET':
        if (!isset($_GET['user_id'])) {
            sendError("User ID required");
        }
        
        $userId = $_GET['user_id'];
        
        $stmt = $db->prepare("
            SELECT s.id as id, s.product_id as product_id, p.title, p.price, p.original_price, 
                   p.rating, p.image, p.source, p.url, s.created_at as created_at
            FROM saved_items s
            JOIN products p ON s.product_id = p.id
            WHERE s.user_id = ?
            ORDER BY s.created_at DESC
        ");
        $stmt->execute([$userId]);
        $items = $stmt->fetchAll();
        
        sendResponse([
            'saved_items' => $items,
            'count' => count($items)
        ]);
        break;
        
    case 'POST':
        $data = getInput();
        validateRequired($data, ['user_id', 'product_id']);
        
        // Check if already saved
        $checkStmt = $db->prepare("SELECT id FROM saved_items WHERE user_id = ? AND product_id = ?");
        $checkStmt->execute([$data['user_id'], $data['product_id']]);
        
        if ($checkStmt->fetch()) {
            sendError("Item already saved");
        }
        
        $stmt = $db->prepare("INSERT INTO saved_items (user_id, product_id) VALUES (?, ?)");
        $stmt->execute([$data['user_id'], $data['product_id']]);
        
        sendResponse(['message' => 'Item saved'], 201);
        break;
        
    case 'DELETE':
        if (!isset($_GET['user_id']) || !isset($_GET['product_id'])) {
            sendError("User ID and Product ID required");
        }
        
        $stmt = $db->prepare("DELETE FROM saved_items WHERE user_id = ? AND product_id = ?");
        $stmt->execute([$_GET['user_id'], $_GET['product_id']]);
        
        if ($stmt->rowCount() > 0) {
            sendResponse(['message' => 'Item removed']);
        } else {
            sendError("Saved item not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
