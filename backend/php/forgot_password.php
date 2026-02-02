<?php
/**
 * Forgot Password - Send OTP via Email
 * Generates OTP and sends to user's registered email
 */

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, PUT");
header("Access-Control-Allow-Headers: Content-Type");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include_once 'db.php';
include_once 'mail_config.php';

$data = json_decode(file_get_contents("php://input"));
$method = $_SERVER['REQUEST_METHOD'];

// Create password_reset_otps table if not exists
$conn->query("CREATE TABLE IF NOT EXISTS password_reset_otps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME DEFAULT NULL,
    is_verified TINYINT(1) DEFAULT 0,
    INDEX idx_email (email),
    INDEX idx_otp (otp_code)
)");

// Determine action from query param or method
$action = isset($_GET['action']) ? $_GET['action'] : '';

if ($method === 'POST') {
    if ($action === 'verify') {
        // === VERIFY OTP ===
        if (empty($data->email) || empty($data->otp)) {
            echo json_encode(["success" => false, "message" => "Email and OTP are required"]);
            exit();
        }
        
        $email = $conn->real_escape_string($data->email);
        $otp = $conn->real_escape_string($data->otp);
        
        // Check OTP
        $query = "SELECT * FROM password_reset_otps WHERE email = '$email' AND otp_code = '$otp' AND is_verified = 0";
        $result = $conn->query($query);
        
        if ($result->num_rows === 0) {
            echo json_encode(["success" => false, "message" => "Invalid OTP code"]);
            exit();
        }
        
        $otpRecord = $result->fetch_assoc();
        
        // Check if OTP expired
        if (strtotime($otpRecord['expires_at']) < time()) {
            echo json_encode(["success" => false, "message" => "OTP has expired. Please request a new one."]);
            exit();
        }
        
        // Mark OTP as verified
        $conn->query("UPDATE password_reset_otps SET is_verified = 1 WHERE id = " . $otpRecord['id']);
        
        echo json_encode([
            "success" => true, 
            "message" => "OTP verified successfully",
            "email" => $email
        ]);
        
    } elseif ($action === 'reset') {
        // === RESET PASSWORD ===
        if (empty($data->email) || empty($data->new_password)) {
            echo json_encode(["success" => false, "message" => "Email and new password are required"]);
            exit();
        }
        
        $email = $conn->real_escape_string($data->email);
        $newPassword = password_hash($data->new_password, PASSWORD_DEFAULT);
        
        // Check if OTP was verified
        $query = "SELECT * FROM password_reset_otps WHERE email = '$email' AND is_verified = 1 ORDER BY created_at DESC LIMIT 1";
        $result = $conn->query($query);
        
        if ($result->num_rows === 0) {
            echo json_encode(["success" => false, "message" => "Please verify OTP first"]);
            exit();
        }
        
        // Update password in users table
        $updateQuery = "UPDATE users SET password = '$newPassword' WHERE email = '$email'";
        if ($conn->query($updateQuery)) {
            // Delete used OTPs
            $conn->query("DELETE FROM password_reset_otps WHERE email = '$email'");
            
            echo json_encode([
                "success" => true, 
                "message" => "Password reset successfully! You can now login with your new password."
            ]);
        } else {
            echo json_encode(["success" => false, "message" => "Failed to reset password"]);
        }
        
    } else {
        // === SEND OTP (default POST action) ===
        if (empty($data->email)) {
            echo json_encode(["success" => false, "message" => "Email is required"]);
            exit();
        }
        
        $email = $conn->real_escape_string($data->email);
        
        // Check if email exists in users table
        $query = "SELECT id, email, name FROM users WHERE email = '$email'";
        $result = $conn->query($query);
        
        if ($result->num_rows === 0) {
            echo json_encode(["success" => false, "message" => "No account found with this email"]);
            exit();
        }
        
        $user = $result->fetch_assoc();
        $userName = $user['name'];
        
        // Generate OTP
        $otp = generateOTP();
        $expiresAt = date('Y-m-d H:i:s', strtotime('+' . OTP_EXPIRY_MINUTES . ' minutes'));
        
        // Delete any existing OTP for this email
        $conn->query("DELETE FROM password_reset_otps WHERE email = '$email'");
        
        // Insert new OTP
        $stmt = $conn->prepare("INSERT INTO password_reset_otps (email, otp_code, expires_at) VALUES (?, ?, ?)");
        $stmt->bind_param("sss", $email, $otp, $expiresAt);
        
        if (!$stmt->execute()) {
            echo json_encode(["success" => false, "message" => "Failed to generate OTP"]);
            exit();
        }
        
        // Send OTP email
        $subject = "Deal Hive - Password Reset Code";
        $body = getPasswordResetEmailTemplate($otp, $userName);
        $emailResult = sendEmail($email, $subject, $body);
        
        if ($emailResult['success']) {
            echo json_encode([
                "success" => true, 
                "message" => "OTP sent to your email",
                "email" => $email
            ]);
        } else {
            // Still return success for testing (if email fails)
            echo json_encode([
                "success" => true, 
                "message" => "OTP generated. Check your email.",
                "email" => $email,
                "debug" => OTP_DEBUG ? "OTP: $otp" : null
            ]);
        }
        
        $stmt->close();
    }
}

$conn->close();

/**
 * Get Password Reset email HTML template
 */
function getPasswordResetEmailTemplate($otp, $userName = 'User') {
    return "
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset='UTF-8'>
        <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    </head>
    <body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #0f0f23;'>
        <table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #1a1a2e;'>
            <tr>
                <td style='padding: 40px 30px; text-align: center; background: linear-gradient(135deg, #00C896 0%, #00A67E 100%);'>
                    <h1 style='color: #ffffff; margin: 0; font-size: 28px;'>🐝 Deal Hive</h1>
                    <p style='color: rgba(255,255,255,0.9); margin: 10px 0 0 0;'>Password Reset</p>
                </td>
            </tr>
            <tr>
                <td style='padding: 40px 30px;'>
                    <h2 style='color: #ffffff; margin: 0 0 20px 0;'>Hello {$userName}!</h2>
                    <p style='color: #b0b0b0; font-size: 16px; line-height: 1.6;'>
                        We received a request to reset your password. Use the following OTP code to proceed:
                    </p>
                    
                    <div style='background-color: #2a2a3e; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0;'>
                        <span style='font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #00C896;'>{$otp}</span>
                    </div>
                    
                    <p style='color: #888888; font-size: 14px;'>
                        This code will expire in <strong>" . OTP_EXPIRY_MINUTES . " minutes</strong>.
                    </p>
                    <p style='color: #888888; font-size: 14px;'>
                        If you didn't request a password reset, please ignore this email.
                    </p>
                </td>
            </tr>
            <tr>
                <td style='padding: 30px; background-color: #0f0f1a; text-align: center;'>
                    <p style='color: #666666; font-size: 12px; margin: 0;'>
                        &copy; 2026 Deal Hive. All rights reserved.
                    </p>
                </td>
            </tr>
        </table>
    </body>
    </html>
    ";
}
?>
