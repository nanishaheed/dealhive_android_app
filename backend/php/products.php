<?php
/**
 * Deal Hive - Products API
 * 
 * Endpoints:
 * GET /products.php - Get all products
 * GET /products.php?id=1 - Get single product
 * GET /products.php?category=smartphones - Get products by category
 * POST /products.php - Create new product
 * PUT /products.php?id=1 - Update product
 * DELETE /products.php?id=1 - Delete product
 */

require_once 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

switch ($method) {
    case 'GET':
        if (isset($_GET['id'])) {
            // Get single product
            $stmt = $db->prepare("SELECT * FROM products WHERE id = ?");
            $stmt->execute([$_GET['id']]);
            $product = $stmt->fetch();
            
            if ($product) {
                // Parse JSON fields
                $product['specs'] = json_decode($product['specs'], true);
                $product['key_features'] = json_decode($product['key_features'], true);
                sendResponse($product);
            } else {
                sendError("Product not found", 404);
            }
        } elseif (isset($_GET['category'])) {
            // Get products by category
            $categorySlug = $_GET['category'];
            
            // First, try to find the category by slug or name
            $catStmt = $db->prepare("SELECT id, name FROM categories WHERE slug = ? OR name = ? LIMIT 1");
            $catStmt->execute([$categorySlug, $categorySlug]);
            $categoryRow = $catStmt->fetch();
            
            if ($categoryRow) {
                // Found category - get products by category_id OR title matching category name
                $categoryId = $categoryRow['id'];
                $categoryName = '%' . $categoryRow['name'] . '%';
                $stmt = $db->prepare("SELECT * FROM products WHERE category_id = ? OR title LIKE ? ORDER BY created_at DESC");
                $stmt->execute([$categoryId, $categoryName]);
            } else {
                // Fallback: search by title containing the category string
                $categorySearch = '%' . $categorySlug . '%';
                $stmt = $db->prepare("SELECT * FROM products WHERE title LIKE ? ORDER BY created_at DESC");
                $stmt->execute([$categorySearch]);
            }
            
            $products = $stmt->fetchAll();
            
            foreach ($products as &$product) {
                $product['specs'] = json_decode($product['specs'], true);
                $product['key_features'] = json_decode($product['key_features'], true);
            }
            
            sendResponse(['products' => $products, 'count' => count($products)]);
        } else {
            // Get all products
            $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
            $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
            
            $stmt = $db->prepare("SELECT * FROM products ORDER BY created_at DESC LIMIT ? OFFSET ?");
            $stmt->execute([$limit, $offset]);
            $products = $stmt->fetchAll();
            
            foreach ($products as &$product) {
                $product['specs'] = json_decode($product['specs'], true);
                $product['key_features'] = json_decode($product['key_features'], true);
            }
            
            // Get total count
            $countStmt = $db->query("SELECT COUNT(*) FROM products");
            $total = $countStmt->fetchColumn();
            
            sendResponse([
                'products' => $products,
                'count' => count($products),
                'total' => (int)$total
            ]);
        }
        break;
        
    case 'POST':
        $data = getInput();
        validateRequired($data, ['source', 'title', 'price', 'url']);
        
        $stmt = $db->prepare("
            INSERT INTO products (source, title, price, original_price, rating, reviews, image, url, ar_model, specs, key_features, category_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ");
        
        $stmt->execute([
            $data['source'],
            $data['title'],
            $data['price'],
            $data['original_price'] ?? null,
            $data['rating'] ?? null,
            $data['reviews'] ?? null,
            $data['image'] ?? null,
            $data['url'],
            $data['ar_model'] ?? null,
            json_encode($data['specs'] ?? []),
            json_encode($data['key_features'] ?? []),
            $data['category_id'] ?? null
        ]);
        
        $productId = $db->lastInsertId();
        
        // Record initial price history
        $priceStmt = $db->prepare("INSERT INTO price_history (product_id, price) VALUES (?, ?)");
        $priceStmt->execute([$productId, $data['price']]);
        
        sendResponse(['message' => 'Product created', 'id' => $productId], 201);
        break;
        
    case 'PUT':
        if (!isset($_GET['id'])) {
            sendError("Product ID required");
        }
        
        $data = getInput();
        $id = $_GET['id'];
        
        // Check if product exists
        $checkStmt = $db->prepare("SELECT id, price FROM products WHERE id = ?");
        $checkStmt->execute([$id]);
        $existing = $checkStmt->fetch();
        
        if (!$existing) {
            sendError("Product not found", 404);
        }
        
        // Build update query dynamically
        $updates = [];
        $params = [];
        
        $fields = ['source', 'title', 'price', 'original_price', 'rating', 'reviews', 'image', 'url', 'ar_model', 'category_id'];
        foreach ($fields as $field) {
            if (isset($data[$field])) {
                $updates[] = "$field = ?";
                $params[] = $data[$field];
            }
        }
        
        if (isset($data['specs'])) {
            $updates[] = "specs = ?";
            $params[] = json_encode($data['specs']);
        }
        
        if (isset($data['key_features'])) {
            $updates[] = "key_features = ?";
            $params[] = json_encode($data['key_features']);
        }
        
        if (empty($updates)) {
            sendError("No fields to update");
        }
        
        $params[] = $id;
        $stmt = $db->prepare("UPDATE products SET " . implode(', ', $updates) . " WHERE id = ?");
        $stmt->execute($params);
        
        // Record price change in history if price changed
        if (isset($data['price']) && $data['price'] != $existing['price']) {
            $priceStmt = $db->prepare("INSERT INTO price_history (product_id, price) VALUES (?, ?)");
            $priceStmt->execute([$id, $data['price']]);
        }
        
        sendResponse(['message' => 'Product updated']);
        break;
        
    case 'DELETE':
        if (!isset($_GET['id'])) {
            sendError("Product ID required");
        }
        
        $stmt = $db->prepare("DELETE FROM products WHERE id = ?");
        $stmt->execute([$_GET['id']]);
        
        if ($stmt->rowCount() > 0) {
            sendResponse(['message' => 'Product deleted']);
        } else {
            sendError("Product not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
