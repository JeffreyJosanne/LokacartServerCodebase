package app.util;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

import app.business.controllers.rest.DashboardRestController;

public class GcmRequest {

	public void broadcast(String userMessage, String title, List<String> androidTargets, int... params) {
		final long serialVersionUID = 1L;
		// List<String> androidTargets = new ArrayList<String>();
		// final String SENDER_ID = "AIzaSyBsUr9b5VT5_oH_0FSygF8us4AXKA1yvGw";
		System.out.println("called");
		final String SENDER_ID = "AIzaSyCij7ogLIlqqAGLLKDrvlgILaimYh1-3KU";
		Sender sender = new Sender(SENDER_ID);
		Message message = null;
		if (params.length == 1) {
			System.out.println("Order notif sent");
			message = new Message.Builder()
					// .timeToLive(30)
					.delayWhileIdle(false).addData("message", userMessage).addData("title", title)
					.addData("id", Integer.toString(params[0])).build();
		} else if (params.length == 2) {
			System.out.println("Memeber notif sent");
			message = new Message.Builder()
					// .timeToLive(30)
					.delayWhileIdle(false).addData("message", userMessage).addData("title", title)
					.addData("id", Integer.toString(params[0])).addData("userId", Integer.toString(params[1])).build();

		}

		try {
			System.out.println("In try");
			MulticastResult result = sender.send(message, androidTargets, 1);
			System.out.println("cleared");
			if (result.getResults() != null) {
				int canonicalRegId = result.getCanonicalIds();
				if (canonicalRegId != 0) {

				}
			} else {
				int error = result.getFailure();
				System.out.println("Broadcast failure: " + error);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public void broadcast(List<String> androidTargets, String orgabbr, HashMap <String, Integer> dashData) {
		final String SENDER_ID = "AIzaSyCij7ogLIlqqAGLLKDrvlgILaimYh1-3KU";
		Sender sender = new Sender(SENDER_ID);
		Message message = null;
		System.out.println("sending dash data");
//		HashMap<String, Integer> dashData = null;
//		try {
//
//			DashboardRestController dashboardRestController = new DashboardRestController();
//			dashData = dashboardRestController.dashBoard(orgabbr);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		message = new Message.Builder()
				// .timeToLive(30)
				.delayWhileIdle(false)
				.addData("id", "2")
				.addData("processed",Integer.toString(dashData.get("processed")))
				.addData("cancelled", Integer.toString(dashData.get("cancelled")))
				.addData("saved", Integer.toBinaryString(dashData.get("saved")))
				.addData("newUsersToday", Integer.toBinaryString(dashData.get("newUsersToday")))
				.addData("pendingUsers", Integer.toBinaryString(dashData.get("pendingUsers")))
				.addData("totalUsers", Integer.toBinaryString(dashData.get("totalUsers")))
				.collapseKey("2")
				.build();
		try {
			System.out.println("In try");
			MulticastResult result = sender.send(message, androidTargets, 1);
			System.out.println("cleared");
			if (result.getResults() != null) {
				int canonicalRegId = result.getCanonicalIds();
				if (canonicalRegId != 0) {

				}
			} else {
				int error = result.getFailure();
				System.out.println("Broadcast failure: " + error);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
