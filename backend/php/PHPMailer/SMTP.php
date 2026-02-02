<?php
/**
 * PHPMailer SMTP class
 * Handles SMTP communication
 */
namespace PHPMailer\PHPMailer;

class SMTP
{
    const VERSION = '6.8.0';
    const CRLF = "\r\n";
    const DEFAULT_PORT = 25;
    const MAX_LINE_LENGTH = 998;
    const MAX_REPLY_LENGTH = 512;
    const DEBUG_OFF = 0;
    const DEBUG_CLIENT = 1;
    const DEBUG_SERVER = 2;
    const DEBUG_CONNECTION = 3;
    const DEBUG_LOWLEVEL = 4;

    public $do_debug = self::DEBUG_OFF;
    public $Debugoutput = 'echo';
    public $do_verp = false;
    public $Timeout = 300;
    public $Timelimit = 300;

    protected $smtp_conn;
    protected $error = [];
    protected $helo_rply = null;
    protected $server_caps = null;
    protected $last_reply = '';

    public function connect($host, $port = null, $timeout = 30, $options = [])
    {
        static $streamok;
        if (null === $streamok) {
            $streamok = function_exists('stream_socket_client');
        }

        $this->smtp_conn = null;
        $this->error = [];
        $this->helo_rply = null;

        $port = $port ?: self::DEFAULT_PORT;
        $this->edebug("Connection: opening to $host:$port, timeout=$timeout, options=" . (count($options) > 0 ? var_export($options, true) : 'array()'), self::DEBUG_CONNECTION);

        $errno = 0;
        $errstr = '';
        if ($streamok) {
            $socket_context = stream_context_create($options);
            $this->smtp_conn = @stream_socket_client(
                $host . ':' . $port,
                $errno,
                $errstr,
                $timeout,
                STREAM_CLIENT_CONNECT,
                $socket_context
            );
        } else {
            $this->edebug('Connection: stream_socket_client not available, falling back to fsockopen', self::DEBUG_CONNECTION);
            $this->smtp_conn = @fsockopen($host, $port, $errno, $errstr, $timeout);
        }

        if (!is_resource($this->smtp_conn)) {
            $this->setError('Failed to connect to server', '', (string) $errno, $errstr);
            $this->edebug('SMTP ERROR: ' . $this->error['error'] . ": $errstr ($errno)", self::DEBUG_CLIENT);
            return false;
        }

        stream_set_timeout($this->smtp_conn, $timeout, 0);

        $announce = $this->get_lines();
        $this->edebug('SERVER -> CLIENT: ' . $announce, self::DEBUG_SERVER);

        return true;
    }

    public function startTLS()
    {
        if (!$this->sendCommand('STARTTLS', 'STARTTLS', 220)) {
            return false;
        }
        $crypto_method = STREAM_CRYPTO_METHOD_TLS_CLIENT;
        if (defined('STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT')) {
            $crypto_method |= STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT;
            $crypto_method |= STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT;
        }
        return stream_socket_enable_crypto($this->smtp_conn, true, $crypto_method);
    }

    public function authenticate($username, $password, $authtype = null, $OAuth = null)
    {
        if (!$this->sendCommand('AUTH LOGIN', 'AUTH LOGIN', 334)) {
            return false;
        }
        if (!$this->sendCommand('Username', base64_encode($username), 334)) {
            return false;
        }
        if (!$this->sendCommand('Password', base64_encode($password), 235)) {
            return false;
        }
        return true;
    }

    public function hello($host = '')
    {
        return $this->sendHello('EHLO', $host) || $this->sendHello('HELO', $host);
    }

    protected function sendHello($hello, $host)
    {
        $noerror = $this->sendCommand($hello, $hello . ' ' . $host, 250);
        $this->helo_rply = $this->last_reply;
        if ($noerror) {
            $this->parseHelloFields($hello);
        }
        return $noerror;
    }

    protected function parseHelloFields($type)
    {
        $this->server_caps = [];
        $lines = explode("\n", $this->helo_rply);
        foreach ($lines as $n => $s) {
            $s = trim(substr($s, 4));
            if (empty($s)) {
                continue;
            }
            $fields = explode(' ', $s);
            if (!empty($fields)) {
                $name = array_shift($fields);
                switch ($name) {
                    case 'SIZE':
                        $this->server_caps['SIZE'] = (int) array_shift($fields);
                        break;
                    case 'AUTH':
                        $this->server_caps['AUTH'] = $fields;
                        break;
                    default:
                        $this->server_caps[$name] = true;
                }
            }
        }
    }

    public function mail($from)
    {
        return $this->sendCommand('MAIL FROM', 'MAIL FROM:<' . $from . '>', 250);
    }

    public function recipient($address, $dsn = '')
    {
        return $this->sendCommand('RCPT TO', 'RCPT TO:<' . $address . '>', [250, 251]);
    }

    public function data($msg_data)
    {
        if (!$this->sendCommand('DATA', 'DATA', 354)) {
            return false;
        }
        $msg_data = str_replace("\r\n", "\n", $msg_data);
        $msg_data = str_replace("\r", "\n", $msg_data);
        $lines = explode("\n", $msg_data);
        $in_headers = true;
        $max_line_length = static::MAX_LINE_LENGTH;

        foreach ($lines as $line) {
            if ($in_headers && $line === '') {
                $in_headers = false;
            }
            if (strlen($line) > $max_line_length) {
                $line = substr($line, 0, $max_line_length);
            }
            if (isset($line[0]) && $line[0] === '.') {
                $line = '.' . $line;
            }
            $this->client_send($line . static::CRLF);
        }
        $this->client_send('.' . static::CRLF);
        $rply = $this->get_lines();
        $this->edebug('SERVER -> CLIENT: ' . $rply, self::DEBUG_SERVER);

        return substr($rply, 0, 3) == '250';
    }

    public function quit($close_on_error = true)
    {
        $this->sendCommand('QUIT', 'QUIT', 221);
        if ($close_on_error) {
            $this->close();
        }
        return true;
    }

    public function close()
    {
        if (is_resource($this->smtp_conn)) {
            fclose($this->smtp_conn);
            $this->smtp_conn = null;
        }
    }

    protected function sendCommand($command, $commandstring, $expect)
    {
        if (!is_array($expect)) {
            $expect = [$expect];
        }
        $this->client_send($commandstring . static::CRLF);
        $this->last_reply = $this->get_lines();
        $this->edebug('SERVER -> CLIENT: ' . $this->last_reply, self::DEBUG_SERVER);

        $reply_code = (int) substr($this->last_reply, 0, 3);
        if (!in_array($reply_code, $expect, true)) {
            $this->setError($command . ' command failed', $this->last_reply, $reply_code);
            return false;
        }
        return true;
    }

    protected function client_send($data)
    {
        $this->edebug('CLIENT -> SERVER: ' . $data, self::DEBUG_CLIENT);
        return fwrite($this->smtp_conn, $data);
    }

    protected function get_lines()
    {
        if (!is_resource($this->smtp_conn)) {
            return '';
        }
        $data = '';
        $endtime = time() + $this->Timelimit;
        stream_set_timeout($this->smtp_conn, $this->Timeout);
        while (is_resource($this->smtp_conn) && !feof($this->smtp_conn)) {
            $str = @fgets($this->smtp_conn, self::MAX_REPLY_LENGTH);
            $data .= $str;
            if (isset($str[3]) && $str[3] === ' ') {
                break;
            }
            $info = stream_get_meta_data($this->smtp_conn);
            if ($info['timed_out']) {
                break;
            }
            if (time() > $endtime) {
                break;
            }
        }
        return $data;
    }

    protected function setError($message, $detail = '', $code = '', $smtpcode = '', $error = '')
    {
        $this->error = [
            'error' => $message,
            'detail' => $detail,
            'smtp_code' => $code,
            'smtp_code_ex' => $smtpcode,
        ];
    }

    public function getError()
    {
        return $this->error;
    }

    protected function edebug($str, $level = 0)
    {
        if ($level > $this->do_debug) {
            return;
        }
        if ($this->Debugoutput instanceof \Closure) {
            call_user_func($this->Debugoutput, $str, $level);
            return;
        }
        if ($this->Debugoutput === 'error_log') {
            error_log($str);
        } elseif ($this->do_debug > 0) {
            echo gmdate('Y-m-d H:i:s') . "\t" . $str . "\n";
        }
    }

    public function connected()
    {
        return is_resource($this->smtp_conn);
    }

    public function reset()
    {
        return $this->sendCommand('RSET', 'RSET', 250);
    }

    public function getServerExtList()
    {
        return $this->server_caps;
    }

    public function getServerExt($name)
    {
        if (empty($this->server_caps)) {
            return null;
        }
        if (!array_key_exists($name, $this->server_caps)) {
            return false;
        }
        return $this->server_caps[$name];
    }
}
