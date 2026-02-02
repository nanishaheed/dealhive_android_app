<?php
/**
 * PHPMailer - PHP email creation and transport class
 * Simplified version for ITFlow
 */
namespace PHPMailer\PHPMailer;

class PHPMailer
{
    const VERSION = '6.8.0';
    const CHARSET_ASCII = 'us-ascii';
    const CHARSET_ISO88591 = 'iso-8859-1';
    const CHARSET_UTF8 = 'utf-8';
    const CONTENT_TYPE_PLAINTEXT = 'text/plain';
    const CONTENT_TYPE_TEXT_CALENDAR = 'text/calendar';
    const CONTENT_TYPE_TEXT_HTML = 'text/html';
    const CONTENT_TYPE_MULTIPART_ALTERNATIVE = 'multipart/alternative';
    const CONTENT_TYPE_MULTIPART_MIXED = 'multipart/mixed';
    const CONTENT_TYPE_MULTIPART_RELATED = 'multipart/related';
    const ENCODING_7BIT = '7bit';
    const ENCODING_8BIT = '8bit';
    const ENCODING_BASE64 = 'base64';
    const ENCODING_BINARY = 'binary';
    const ENCODING_QUOTED_PRINTABLE = 'quoted-printable';
    const ENCRYPTION_STARTTLS = 'tls';
    const ENCRYPTION_SMTPS = 'ssl';
    const ICAL_METHOD_REQUEST = 'REQUEST';
    const ICAL_METHOD_PUBLISH = 'PUBLISH';
    const ICAL_METHOD_REPLY = 'REPLY';
    const ICAL_METHOD_ADD = 'ADD';
    const ICAL_METHOD_CANCEL = 'CANCEL';
    const ICAL_METHOD_REFRESH = 'REFRESH';
    const ICAL_METHOD_COUNTER = 'COUNTER';
    const ICAL_METHOD_DECLINECOUNTER = 'DECLINECOUNTER';

    public $Priority;
    public $CharSet = self::CHARSET_UTF8;
    public $ContentType = self::CONTENT_TYPE_PLAINTEXT;
    public $Encoding = self::ENCODING_8BIT;
    public $ErrorInfo = '';
    public $From = '';
    public $FromName = '';
    public $Sender = '';
    public $Subject = '';
    public $Body = '';
    public $AltBody = '';
    public $Ical = '';
    public $MIMEBody = '';
    public $MIMEHeader = '';
    public $mailHeader = '';
    public $WordWrap = 0;
    public $Mailer = 'mail';
    public $Sendmail = '/usr/sbin/sendmail';
    public $UseSendmailOptions = true;
    public $Host = 'localhost';
    public $Port = 25;
    public $Helo = '';
    public $SMTPSecure = '';
    public $SMTPAutoTLS = true;
    public $SMTPAuth = false;
    public $SMTPOptions = [];
    public $Username = '';
    public $Password = '';
    public $AuthType = '';
    public $oauth;
    public $Timeout = 300;
    public $dsn = '';
    public $SMTPDebug = 0;
    public $Debugoutput = 'echo';
    public $SMTPKeepAlive = false;
    public $SingleTo = false;
    public $SingleToArray = [];
    public $do_verp = false;
    public $AllowEmpty = false;
    public $DKIM_selector = '';
    public $DKIM_identity = '';
    public $DKIM_passphrase = '';
    public $DKIM_domain = '';
    public $DKIM_copyHeaderFields = true;
    public $DKIM_extraHeaders = [];
    public $DKIM_private = '';
    public $DKIM_private_string = '';
    public $action_function = '';
    public $XMailer = '';
    public $Hostname = '';
    public $MessageID = '';
    public $MessageDate = '';

    protected $to = [];
    protected $cc = [];
    protected $bcc = [];
    protected $ReplyTo = [];
    protected $all_recipients = [];
    protected $RecipientsQueue = [];
    protected $ReplyToQueue = [];
    protected $attachment = [];
    protected $CustomHeader = [];
    protected $lastMessageID = '';
    protected $message_type = '';
    protected $boundary = [];
    protected $language = [];
    protected $error_count = 0;
    protected $sign_cert_file = '';
    protected $sign_key_file = '';
    protected $sign_extracerts_file = '';
    protected $sign_key_pass = '';
    protected $exceptions = false;
    protected $uniqueid = '';

    protected $smtp;

    public function __construct($exceptions = null)
    {
        if (null !== $exceptions) {
            $this->exceptions = (bool) $exceptions;
        }
        $this->Debugoutput = function ($str, $level) {
            error_log("PHPMailer Debug: $str");
        };
    }

    public function __destruct()
    {
        $this->smtpClose();
    }

    public function isSMTP()
    {
        $this->Mailer = 'smtp';
    }

    public function isMail()
    {
        $this->Mailer = 'mail';
    }

    public function setFrom($address, $name = '', $auto = true)
    {
        $address = trim($address);
        $name = trim(preg_replace('/[\r\n]+/', '', $name));
        $this->From = $address;
        $this->FromName = $name;
        if ($auto && empty($this->Sender)) {
            $this->Sender = $address;
        }
        return true;
    }

    public function addAddress($address, $name = '')
    {
        return $this->addAnAddress('to', $address, $name);
    }

    public function addCC($address, $name = '')
    {
        return $this->addAnAddress('cc', $address, $name);
    }

    public function addBCC($address, $name = '')
    {
        return $this->addAnAddress('bcc', $address, $name);
    }

    public function addReplyTo($address, $name = '')
    {
        return $this->addAnAddress('Reply-To', $address, $name);
    }

    protected function addAnAddress($kind, $address, $name = '')
    {
        $address = trim($address);
        $name = trim(preg_replace('/[\r\n]+/', '', $name));
        $pos = strrpos($address, '@');
        if ($pos === false) {
            $error_message = 'Invalid address (no @ symbol): ' . $address;
            $this->setError($error_message);
            if ($this->exceptions) {
                throw new Exception($error_message);
            }
            return false;
        }
        if (!isset($this->all_recipients[strtolower($address)])) {
            switch ($kind) {
                case 'to':
                    $this->to[] = [$address, $name];
                    break;
                case 'cc':
                    $this->cc[] = [$address, $name];
                    break;
                case 'bcc':
                    $this->bcc[] = [$address, $name];
                    break;
                case 'Reply-To':
                    $this->ReplyTo[] = [$address, $name];
                    break;
            }
            $this->all_recipients[strtolower($address)] = true;
        }
        return true;
    }

    public function isHTML($isHtml = true)
    {
        if ($isHtml) {
            $this->ContentType = self::CONTENT_TYPE_TEXT_HTML;
        } else {
            $this->ContentType = self::CONTENT_TYPE_PLAINTEXT;
        }
    }

    public function send()
    {
        try {
            if (!$this->preSend()) {
                return false;
            }
            return $this->postSend();
        } catch (Exception $exc) {
            $this->mailHeader = '';
            $this->setError($exc->getMessage());
            if ($this->exceptions) {
                throw $exc;
            }
            return false;
        }
    }

    public function preSend()
    {
        if (empty($this->to) && empty($this->cc) && empty($this->bcc)) {
            throw new Exception('You must provide at least one recipient email address.');
        }
        if (empty($this->From)) {
            throw new Exception('You must provide a sender address.');
        }
        
        $this->setMessageType();
        $this->MIMEHeader = $this->createHeader();
        $this->MIMEBody = $this->createBody();
        
        return true;
    }

    public function postSend()
    {
        switch ($this->Mailer) {
            case 'smtp':
                return $this->smtpSend($this->MIMEHeader, $this->MIMEBody);
            case 'mail':
            default:
                return $this->mailSend($this->MIMEHeader, $this->MIMEBody);
        }
    }

    protected function smtpSend($header, $body)
    {
        if (!$this->smtpConnect()) {
            throw new Exception('SMTP connect failed');
        }

        $smtp_from = $this->Sender ?: $this->From;
        if (!$this->smtp->mail($smtp_from)) {
            $this->setError('SMTP Error: Could not authenticate.');
            throw new Exception('SMTP FROM command failed: ' . implode(', ', $this->smtp->getError()));
        }

        foreach ($this->to as $toaddr) {
            if (!$this->smtp->recipient($toaddr[0])) {
                throw new Exception('SMTP RCPT command failed for: ' . $toaddr[0]);
            }
        }
        foreach ($this->cc as $ccaddr) {
            if (!$this->smtp->recipient($ccaddr[0])) {
                throw new Exception('SMTP RCPT command failed for: ' . $ccaddr[0]);
            }
        }
        foreach ($this->bcc as $bccaddr) {
            if (!$this->smtp->recipient($bccaddr[0])) {
                throw new Exception('SMTP RCPT command failed for: ' . $bccaddr[0]);
            }
        }

        if (!$this->smtp->data($header . "\r\n\r\n" . $body)) {
            throw new Exception('SMTP DATA command failed');
        }

        if (!$this->SMTPKeepAlive) {
            $this->smtpClose();
        }

        return true;
    }

    public function smtpConnect($options = null)
    {
        if (null === $this->smtp) {
            $this->smtp = new SMTP();
        }

        if ($this->smtp->connected()) {
            return true;
        }

        $this->smtp->Timeout = $this->Timeout;
        $this->smtp->do_debug = $this->SMTPDebug;
        $this->smtp->Debugoutput = $this->Debugoutput;
        $this->smtp->do_verp = $this->do_verp;

        $hosts = explode(';', $this->Host);
        $lastexception = null;

        foreach ($hosts as $hostentry) {
            $hostinfo = [];
            $host = $hostentry;
            $port = $this->Port;
            $tls = ($this->SMTPSecure === static::ENCRYPTION_STARTTLS);
            $ssl = ($this->SMTPSecure === static::ENCRYPTION_SMTPS);

            if ($ssl) {
                $host = 'ssl://' . $host;
            }

            if ($this->smtp->connect($host, $port, $this->Timeout, $options ?: $this->SMTPOptions)) {
                $hello = !empty($this->Helo) ? $this->Helo : $this->serverHostname();
                $this->smtp->hello($hello);

                if ($tls) {
                    if (!$this->smtp->startTLS()) {
                        throw new Exception('STARTTLS failed');
                    }
                    $this->smtp->hello($hello);
                }

                if ($this->SMTPAuth) {
                    if (!$this->smtp->authenticate($this->Username, $this->Password, $this->AuthType, $this->oauth)) {
                        throw new Exception('SMTP Authentication failed. Error: ' . implode(', ', $this->smtp->getError()));
                    }
                }

                return true;
            }
        }

        $this->smtp->close();
        throw new Exception('SMTP connect() failed.');
    }

    public function smtpClose()
    {
        if (null !== $this->smtp && $this->smtp->connected()) {
            $this->smtp->quit();
            $this->smtp->close();
        }
    }

    protected function mailSend($header, $body)
    {
        $to = '';
        $toArr = [];
        foreach ($this->to as $toaddr) {
            $toArr[] = $this->addrFormat($toaddr);
        }
        $to = implode(', ', $toArr);

        $params = '';
        if (!empty($this->Sender) && strlen(ini_get('safe_mode')) < 1) {
            $params = sprintf('-f%s', $this->Sender);
        }

        $result = @mail($to, $this->encodeHeader($this->Subject), $body, $header, $params);
        if (!$result) {
            throw new Exception('Could not instantiate mail function.');
        }
        return true;
    }

    protected function createHeader()
    {
        $result = '';
        $result .= $this->headerLine('Date', $this->MessageDate ?: date('r'));
        $result .= $this->headerLine('From', $this->addrFormat([$this->From, $this->FromName]));
        
        if (!empty($this->to)) {
            $toArr = [];
            foreach ($this->to as $toaddr) {
                $toArr[] = $this->addrFormat($toaddr);
            }
            $result .= $this->headerLine('To', implode(', ', $toArr));
        }
        
        if (!empty($this->cc)) {
            $ccArr = [];
            foreach ($this->cc as $ccaddr) {
                $ccArr[] = $this->addrFormat($ccaddr);
            }
            $result .= $this->headerLine('Cc', implode(', ', $ccArr));
        }
        
        if (!empty($this->ReplyTo)) {
            $replyArr = [];
            foreach ($this->ReplyTo as $replyaddr) {
                $replyArr[] = $this->addrFormat($replyaddr);
            }
            $result .= $this->headerLine('Reply-To', implode(', ', $replyArr));
        }
        
        $result .= $this->headerLine('Subject', $this->encodeHeader($this->Subject));
        
        if (!empty($this->MessageID)) {
            $result .= $this->headerLine('Message-ID', $this->MessageID);
        } else {
            $result .= $this->headerLine('Message-ID', $this->createMessageID());
        }
        
        $result .= $this->headerLine('X-Mailer', $this->XMailer ?: 'PHPMailer ' . self::VERSION . ' (https://github.com/PHPMailer/PHPMailer)');
        $result .= $this->headerLine('MIME-Version', '1.0');
        $result .= $this->headerLine('Content-Type', $this->ContentType . '; charset=' . $this->CharSet);
        $result .= $this->headerLine('Content-Transfer-Encoding', $this->Encoding);
        
        return $result;
    }

    protected function createBody()
    {
        $body = '';
        if ($this->ContentType === self::CONTENT_TYPE_TEXT_HTML) {
            $body = $this->Body;
        } else {
            $body = $this->Body;
        }
        return $body;
    }

    protected function headerLine($name, $value)
    {
        return $name . ': ' . $value . "\r\n";
    }

    protected function addrFormat($addr)
    {
        if (empty($addr[1])) {
            return $addr[0];
        }
        return $this->encodeHeader($addr[1]) . ' <' . $addr[0] . '>';
    }

    public function encodeHeader($str, $position = 'text')
    {
        $encoded = trim($str);
        if (preg_match('/[\200-\377]/', $str)) {
            $encoded = '=?' . $this->CharSet . '?B?' . base64_encode($str) . '?=';
        }
        return $encoded;
    }

    protected function createMessageID()
    {
        $this->lastMessageID = sprintf(
            '<%s.%s@%s>',
            base_convert((string) time(), 10, 36),
            base_convert((string) random_int(10000, 99999), 10, 36),
            $this->serverHostname()
        );
        return $this->lastMessageID;
    }

    protected function serverHostname()
    {
        if (!empty($this->Hostname)) {
            return $this->Hostname;
        }
        if (isset($_SERVER['SERVER_NAME'])) {
            return $_SERVER['SERVER_NAME'];
        }
        return 'localhost.localdomain';
    }

    protected function setMessageType()
    {
        $this->message_type = [];
        if (!empty($this->AltBody)) {
            $this->message_type[] = 'alt';
        }
        if (!empty($this->attachment)) {
            $this->message_type[] = 'attachments';
        }
    }

    public function setError($msg)
    {
        ++$this->error_count;
        $this->ErrorInfo = $msg;
    }

    public function clearAddresses()
    {
        $this->to = [];
        $this->cc = [];
        $this->bcc = [];
        $this->all_recipients = [];
    }

    public function clearAllRecipients()
    {
        $this->clearAddresses();
        $this->ReplyTo = [];
    }

    public function clearAttachments()
    {
        $this->attachment = [];
    }

    public function clearCustomHeaders()
    {
        $this->CustomHeader = [];
    }
}
