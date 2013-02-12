package com.intradev.cerberus.web.client;

import java.util.Date;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.intradev.cerberus.web.client.controls.Popover;

public class OnboardingPopupManager {

	
	private boolean hasFirstTimeCookie() {		
		String cookie=Cookies.getCookie("firsttime");
		if (cookie == null) {			
			return false;
		}
		return true;
	}
	
	private boolean firstTime=false;
	
	private static OnboardingPopupManager instance;
	
	public static OnboardingPopupManager getOnboardingPopupManager() {
		if (instance == null) {
			instance = new OnboardingPopupManager();			
		}
		return instance;
	}
	
	private OnboardingPopupManager() {
		if (hasFirstTimeCookie() == false) {
//			Calendar cal=Calendar.getInstance();
//			cal.add(Calendar.MINUTE, 1);
			Date date=new Date();
			CalendarUtil.addDaysToDate(date, 1);
			Cookies.setCookie("firsttime","set",date);
			firstTime = true;
		} else {
			//TODO: this can be a debug mode
			firstTime = false;
		}
		
	}
	
	public Popover showWhyLogin(Widget downfrom) {
		return showPanel(downfrom,"Why is this needed?", "Cerberus is currently in beta mode. The final release will have a guest mode for your enjoyment. In the meantime, logging in via your google ID will allow the app to retain any private notes you take as well as to seamlessly synchronize with your Android device.");
	}

	public Popover showWhyKeypass(Widget downfrom) {	
		return showPanel(downfrom,"Why is this needed?", "Cerberus secures all your passwords using a single password you supply. The passcode is only stored in the current browser session and is never sent anywhere. For this reason, the passcode cannot be recovered if you forget it. Be careful!");
	}
	

	private Popover showPanel(Widget downfrom, String title, String text) {
		final RootPanel containerPanel = RootPanel.get(null);

		if (firstTime) {
			return Popover.createPopover(null,containerPanel,downfrom,title, text);
		}
		return null;
	}
	
}
