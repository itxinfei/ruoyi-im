package com.ruoyi.system.exception;

/**
 * IM系统业务异常
 * 
 * @author ruoyi
 */
public class ImException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private int code;

    public ImException(String message)
    {
        super(message);
        this.code = 500;
    }

    public ImException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public ImException(String message, Throwable cause)
    {
        super(message, cause);
        this.code = 500;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }
}
