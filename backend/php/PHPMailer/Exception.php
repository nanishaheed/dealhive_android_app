<?php
/**
 * PHPMailer Exception class
 * Simplified version for ITFlow
 */
namespace PHPMailer\PHPMailer;

class Exception extends \Exception
{
    public function errorMessage()
    {
        return '<strong>' . htmlspecialchars($this->getMessage(), ENT_COMPAT | ENT_HTML401) . "</strong><br />\n";
    }
}
