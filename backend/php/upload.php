<?php
/**
 * Deal Hive - Image Upload API
 * 
 * Endpoints:
 * POST /upload.php - Upload an image
 * 
 * Returns the URL of the uploaded image
 */

require_once 'db.php';

$method = $_SERVER['REQUEST_METHOD'];

// Create uploads directory if it doesn't exist
$uploadDir = __DIR__ . '/uploads/';
if (!file_exists($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

switch ($method) {
    case 'POST':
        // Check if file was uploaded
        if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
            $errorMsg = isset($_FILES['image']) ? "Upload error code: " . $_FILES['image']['error'] : "No file uploaded";
            sendError($errorMsg);
        }
        
        $file = $_FILES['image'];
        
        // Get file extension
        $extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
        
        // Validate file type - images AND 3D models
        $allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        $allowed3DExtensions = ['glb', 'gltf', 'usdz'];
        
        $finfo = finfo_open(FILEINFO_MIME_TYPE);
        $mimeType = finfo_file($finfo, $file['tmp_name']);
        finfo_close($finfo);
        
        $is3DModel = in_array($extension, $allowed3DExtensions);
        $isImage = in_array($mimeType, $allowedImageTypes);
        
        if (!$isImage && !$is3DModel) {
            sendError("Invalid file type. Allowed: JPG, PNG, GIF, WebP, GLB, GLTF, USDZ");
        }
        
        // Validate file size (max 5MB for images, 50MB for 3D models)
        $maxSize = $is3DModel ? 50 * 1024 * 1024 : 5 * 1024 * 1024;
        if ($file['size'] > $maxSize) {
            $maxMB = $is3DModel ? 50 : 5;
            sendError("File too large. Maximum size: {$maxMB}MB");
        }
        
        // Generate unique filename with original extension
        if (empty($extension)) {
            // Get extension from mime type for images
            $extensions = [
                'image/jpeg' => 'jpg',
                'image/png' => 'png',
                'image/gif' => 'gif',
                'image/webp' => 'webp'
            ];
            $extension = $extensions[$mimeType] ?? 'jpg';
        }
        $prefix = $is3DModel ? 'model_' : 'img_';
        $filename = $prefix . uniqid() . '_' . time() . '.' . $extension;
        $filepath = $uploadDir . $filename;
        
        // Move uploaded file
        if (move_uploaded_file($file['tmp_name'], $filepath)) {
            // Generate URL for the uploaded image
            $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
            $host = $_SERVER['HTTP_HOST'];
            $basePath = dirname($_SERVER['SCRIPT_NAME']);
            $imageUrl = $protocol . '://' . $host . $basePath . '/uploads/' . $filename;
            
            sendResponse([
                'message' => 'Image uploaded successfully',
                'url' => $imageUrl,
                'filename' => $filename
            ], 201);
        } else {
            sendError("Failed to save uploaded file");
        }
        break;
        
    case 'GET':
        // List uploaded images (for admin)
        $images = [];
        if (is_dir($uploadDir)) {
            $files = scandir($uploadDir);
            foreach ($files as $file) {
                if ($file !== '.' && $file !== '..' && is_file($uploadDir . $file)) {
                    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
                    $host = $_SERVER['HTTP_HOST'];
                    $basePath = dirname($_SERVER['SCRIPT_NAME']);
                    $images[] = [
                        'filename' => $file,
                        'url' => $protocol . '://' . $host . $basePath . '/uploads/' . $file,
                        'size' => filesize($uploadDir . $file)
                    ];
                }
            }
        }
        sendResponse(['images' => $images, 'count' => count($images)]);
        break;
        
    case 'DELETE':
        if (!isset($_GET['filename'])) {
            sendError("Filename required");
        }
        
        $filename = basename($_GET['filename']); // Prevent directory traversal
        $filepath = $uploadDir . $filename;
        
        if (file_exists($filepath) && is_file($filepath)) {
            if (unlink($filepath)) {
                sendResponse(['message' => 'Image deleted']);
            } else {
                sendError("Failed to delete image");
            }
        } else {
            sendError("Image not found", 404);
        }
        break;
        
    default:
        sendError("Method not allowed", 405);
}
?>
