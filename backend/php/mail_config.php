<?php
/**
 * Mail Configuration for Deal Hive
 * Uses PHPMailer for sending emails
 */

// Include PHPMailer classes (no Composer required)
require_once __DIR__ . '/PHPMailer/Exception.php';
require_once __DIR__ . '/PHPMailer/PHPMailer.php';
require_once __DIR__ . '/PHPMailer/SMTP.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

// SMTP Configuration - Update with your email credentials
define('SMTP_HOST', 'smtp.gmail.com');          // SMTP server host
define('SMTP_PORT', 587);                        // SMTP port (587 for TLS, 465 for SSL)
define('SMTP_USERNAME', '201fa04327teja@gmail.com');     // Your email address
define('SMTP_PASSWORD', 'ajyo zeax blzl lhvp'); // Your app password
define('SMTP_FROM_EMAIL', '201fa04327teja@gmail.com');
define('SMTP_FROM_NAME', 'Deal Hive');
define('SMTP_ENCRYPTION', PHPMailer::ENCRYPTION_STARTTLS); // TLS encryption

// OTP Configuration
define('OTP_EXPIRY_MINUTES', 10);
define('OTP_LENGTH', 6);
define('OTP_DEBUG', true);  // Set to false in production

/**
 * Send email using PHPMailer
 * 
 * @param string $to Recipient email
 * @param string $subject Email subject
 * @param string $body HTML body content
 * @return array ['success' => bool, 'message' => string]
 */
function sendEmail($to, $subject, $body) {
    $mail = new PHPMailer(true);
    
    try {
        // Server settings
        $mail->isSMTP();
        $mail->Host = SMTP_HOST;
        $mail->SMTPAuth = true;
        $mail->Username = SMTP_USERNAME;
        $mail->Password = SMTP_PASSWORD;
        $mail->SMTPSecure = SMTP_ENCRYPTION;
        $mail->Port = SMTP_PORT;
        
        // Recipients
        $mail->setFrom(SMTP_FROM_EMAIL, SMTP_FROM_NAME);
        $mail->addAddress($to);
        
        // Content
        $mail->isHTML(true);
        $mail->Subject = $subject;
        $mail->Body = $body;
        $mail->AltBody = strip_tags($body);
        
        $mail->send();
        return ['success' => true, 'message' => 'Email sent successfully'];
    } catch (Exception $e) {
        return ['success' => false, 'message' => 'Email failed: ' . $mail->ErrorInfo];
    }
}

/**
 * Generate OTP code
 * 
 * @return string 6-digit OTP
 */
function generateOTP() {
    return str_pad(random_int(0, 999999), OTP_LENGTH, '0', STR_PAD_LEFT);
}

/**
 * Get OTP email HTML template
 * 
 * @param string $otp The OTP code
 * @param string $userType 'user' or 'vendor'
 * @return string HTML content
 */
function getOTPEmailTemplate($otp, $userType) {
    $userTypeDisplay = ucfirst($userType);
    
    return "
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset='UTF-8'>
        <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    </head>
    <body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f7f9fc;'>
        <table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #ffffff;'>
            <tr>
                <td style='padding: 40px 30px; text-align: center; background: linear-gradient(135deg, #2563EB 0%, #1d4ed8 100%);'>
                    <h1 style='color: #ffffff; margin: 0; font-size: 28px;'>ITFlow</h1>
                    <p style='color: rgba(255,255,255,0.9); margin: 10px 0 0 0;'>Email Verification</p>
                </td>
            </tr>
            <tr>
                <td style='padding: 40px 30px;'>
                    <h2 style='color: #1f2937; margin: 0 0 20px 0;'>Verify Your Email</h2>
                    <p style='color: #4b5563; font-size: 16px; line-height: 1.6;'>
                        Thank you for registering as a <strong>{$userTypeDisplay}</strong> on ITFlow. 
                        Please use the following OTP code to verify your email address:
                    </p>
                    
                    <div style='background-color: #f3f4f6; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0;'>
                        <span style='font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #2563EB;'>{$otp}</span>
                    </div>
                    
                    <p style='color: #6b7280; font-size: 14px;'>
                        This code will expire in <strong>" . OTP_EXPIRY_MINUTES . " minutes</strong>.
                    </p>
                    <p style='color: #6b7280; font-size: 14px;'>
                        If you didn't request this verification, please ignore this email.
                    </p>
                </td>
            </tr>
            <tr>
                <td style='padding: 30px; background-color: #f9fafb; text-align: center;'>
                    <p style='color: #9ca3af; font-size: 12px; margin: 0;'>
                        &copy; 2026 ITFlow. All rights reserved.
                    </p>
                </td>
            </tr>
        </table>
    </body>
    </html>
    ";
}
?>
