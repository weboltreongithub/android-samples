package com.sample_service;

import java.text.DateFormat;
import java.util.Date;

public class NotificationItem
{
	public String message;

	public NotificationItem(){
		this.message = DateFormat.getTimeInstance().format(new Date() );
	}

	@Override
	public String toString()
	{
		return message;
	}
}
