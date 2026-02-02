<?php
/**
 * Deal Hive - Comparisons API
 * 
 * Endpoints:
 * GET /comparisons.php?user_id=1 - Get user's comparison history
 * POST /comparisons.php - Save a comparison
 * DELETE /comparisons.php?id=1 - Delete a comparison
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
            SELECT c.id, c.ai_verdict, c.created_at,
                   p1.id as product1_id, p1.title as product1_title, p1.price as product1_price, p1.image as product1_image,
                   p2.id as product2_id, p2.title as product2_title, p2.price as product2_price, p2.image as product2_image,
                   pw.id as winner_id, pw.title as winner_title
            FROM comparisons c
            JOIN products p1 ON c.product1_id = p1.id
            JOIN products p2 ON c.product2_id = p2.id
            LEFT JOIN products pw ON c.winner_id = pw.id
            WHERE c.user_id = ?
            ORDER BY c.created_at DESC
        ");
        $stmt->execute([$userId]);
        $comparisons = $stmt->fetchAll();
        
        sendResponse([
            'comparisons' => $comparisons,
            'count' => count($comparisons)
        ]);
        break;
        
    case 'POST':
        $data = getInput();
        validateRequired($data, ['user_id', 'product1_id', 'product2_id']);
        
        $stmt = $db->prepare("
            INSERT INTO comparisons (user_id, product1_id, product2_id, winner_id, ai_verdict) 
            VALUES (?, ?, ?, ?, ?)
        ");
        $stmt->execute([
            $data['user_id'],
            $data['product1_id'],
            $data['product2_id'],
            $data['winner_id'] ?? null,
            $data['ai_verdict'] ?? null
        ]);
        
        $comparisonId = $db->lastInsertId();
        sendResponse(['message' => 'Comparison saved', 'id' => $comparisonId], 201);
        break;
        
    case 'DELETE':
        if (!isset($_GET['id'])) {
            sendError("Comparison ID required");
        }
        
        $stmt = $db->prepare("DELETE FROM comparisons WHERE id = ?");
        $stmt->execute([$_GET['id']]);
        
        if ($stmt->rowCount() > 0) {
            sendResponse(['message' => 'Comparison deleted']);
        } else {
            sendError("Comparison not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
