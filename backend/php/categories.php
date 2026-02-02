<?php
/**
 * Deal Hive - Categories API
 * 
 * Endpoints:
 * GET /categories.php - Get all categories
 * GET /categories.php?slug=smartphones - Get category by slug
 * POST /categories.php - Create new category
 * PUT /categories.php?id=1 - Update category
 * DELETE /categories.php?id=1 - Delete category
 */

require_once 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

switch ($method) {
    case 'GET':
        if (isset($_GET['slug'])) {
            // Get single category by slug
            $stmt = $db->prepare("SELECT * FROM categories WHERE slug = ?");
            $stmt->execute([$_GET['slug']]);
            $category = $stmt->fetch();
            
            if ($category) {
                sendResponse($category);
            } else {
                sendError("Category not found", 404);
            }
        } else {
            // Get all categories with product counts
            // Count products that have category_id matching OR title containing category name
            $stmt = $db->query("
                SELECT c.*, 
                    (SELECT COUNT(*) FROM products p WHERE p.category_id = c.id OR p.title LIKE CONCAT('%', c.name, '%')) as product_count
                FROM categories c 
                ORDER BY c.name ASC
            ");
            $categories = $stmt->fetchAll();
            
            sendResponse([
                'categories' => $categories,
                'count' => count($categories)
            ]);
        }
        break;
        
    case 'POST':
        $data = getInput();
        validateRequired($data, ['name', 'slug']);
        
        $stmt = $db->prepare("
            INSERT INTO categories (name, slug, image) 
            VALUES (?, ?, ?)
        ");
        $stmt->execute([
            $data['name'],
            $data['slug'],
            $data['image'] ?? null
        ]);
        
        $categoryId = $db->lastInsertId();
        sendResponse(['message' => 'Category created', 'id' => $categoryId], 201);
        break;
        
    case 'PUT':
        if (!isset($_GET['id'])) {
            sendError("Category ID required");
        }
        
        $data = getInput();
        $id = $_GET['id'];
        
        $updates = [];
        $params = [];
        
        if (isset($data['name'])) {
            $updates[] = "name = ?";
            $params[] = $data['name'];
        }
        if (isset($data['slug'])) {
            $updates[] = "slug = ?";
            $params[] = $data['slug'];
        }
        if (isset($data['image'])) {
            $updates[] = "image = ?";
            $params[] = $data['image'];
        }
        
        if (empty($updates)) {
            sendError("No fields to update");
        }
        
        $params[] = $id;
        $stmt = $db->prepare("UPDATE categories SET " . implode(', ', $updates) . " WHERE id = ?");
        $stmt->execute($params);
        
        sendResponse(['message' => 'Category updated']);
        break;
        
    case 'DELETE':
        if (!isset($_GET['id'])) {
            sendError("Category ID required");
        }
        
        $stmt = $db->prepare("DELETE FROM categories WHERE id = ?");
        $stmt->execute([$_GET['id']]);
        
        if ($stmt->rowCount() > 0) {
            sendResponse(['message' => 'Category deleted']);
        } else {
            sendError("Category not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
