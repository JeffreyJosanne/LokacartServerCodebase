package app.business.controllers;

import in.ac.iitb.ivrs.telephony.base.IVRSession;
import in.ac.iitb.ivrs.telephony.base.IVRSessionFactory;
import in.ac.iitb.ivrs.telephony.base.util.IVRUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import app.business.services.BroadcastRecipientService;
import app.business.services.BroadcastScheduleService;
import app.business.services.OutboundCallService;
import app.business.services.broadcast.BroadcastService;
import app.entities.BroadcastRecipient;
import app.entities.BroadcastSchedule;
import app.entities.OutboundCall;
import app.entities.broadcast.Broadcast;
import app.telephony.RuralictSession;

@Controller
public class BroadcastCallHandlerController  implements IVRSessionFactory {


	@Autowired
	OutboundCallService outboundCallService;
	
	@Autowired
	BroadcastScheduleService broadcastscheduleservice;
	
	@Autowired
	BroadcastRecipientService broadcastrecipientservice;
	
	@Autowired
	BroadcastService broadcastService;

	/**
	 * Create a new IVR session.
	 * @return The new Ruralict session.
	 */


	@Override
	public IVRSession createSession(String sessionId, String userNumber, String ivrNumber, String circle, String operator) throws Exception {

		RuralictSession ruralictSession = new RuralictSession(sessionId, userNumber, ivrNumber, circle, operator);
		ruralictSession.setOutbound(true);
		return ruralictSession;

	}

	/**
	 * Prints a parameter map to the log for a request.
	 * @param map The map to be printed.
	 */
	private void printParameterMap(Map<String, String[]> map) {
		for (Entry<String, String[]> entry : map.entrySet())
		{
			StringBuilder builder = new StringBuilder();
			builder.append(entry.getKey() + ": ");
			for (String s : entry.getValue())
				builder.append("[" + s + "], ");
			System.out.println(builder.toString());
		}
	}

	/*
	 * GET request received in CallHandler
	 * event: [NewCall], 
	 * cid: [9924914962], 
	 * called_number: [912030157457], 
	 * sid: [8161983673827046], 
	 * circle: [GUJARAT], 
	 * operator: [Idea], 
	 * cid_type: [MOBILE], 
	 * cid_e164: [+919924914962], 
	 * 
	 * GET request received in CallHandler
	 * event: [Record], 
	 * sid: [6161420211007859], 
	 * data: [http://recordings.kookoo.in/vishwajeet/message.wav], 
	 * status: [silence], 
	 * rec_md5_checksum: [755978ffa7052b459ab8c9f46328eae1], 
	 * record_duration: [9], 
	 * cid: [09924914962], 
	 * called_number: [912030157457], 
	 * 
	 * GET request received in CallHandler
	 * event: [Hangup],
	 * sid: [9162022412716778],
	 * process: [none],
	 * total_call_duration: [65],
	 * cid: [9924914962],
	 * called_number: [912030157457],
	 * 
	 * GET request received in CallHandler
	 * event: [Disconnect], 
	 * sid: [9162022505016783], 
	 * process: [none], 
	 * message: [-1], 
	 * total_call_duration: [47], 
	 * cid: [9924914962], 
	 * called_number: [912030157457], 
	 */
	@RequestMapping(value="/BroadcastCallHandler", method=RequestMethod.GET)
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		printParameterMap(request.getParameterMap());

		try {
			response.getOutputStream().println(IVRUtils.doCallHandling(request.getParameterMap(), request.getSession(), this));

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/*
	 * POST request received in CallHandler
	 * sid: [9161419837392183], 
	 * caller_id: [912030157457], 
	 * phone_no: [9773232509], 
	 * duration: [0], 
	 * start_time: [2014-12-29 12:46:32], 
	 * status: [answered], 
	 * status_details: [Normal], 
	 * ringing_time: [12], 
	 */
	@RequestMapping(value="/BroadcastCallHandler", method=RequestMethod.POST)
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try{
		printParameterMap(request.getParameterMap());
		String kookoo_id=request.getParameter("sid");
		String status = request.getParameter("status");
		String statusDetails = request.getParameter("status_details");
		//OutboundCall outboundCall=new OutboundCall();
		Broadcast broadcast= broadcastService.getLastBroadcast();
		List<BroadcastRecipient> broadcastrcpts=broadcastrecipientservice.getBroadcastRecipientByBroadcast(broadcast);
		BroadcastSchedule broadcastsch=broadcastscheduleservice.getBroadcastScheduleByBroadcastId(broadcast, broadcast.getBroadcastedTime());
		for (BroadcastRecipient broadcastrcpt: broadcastrcpts)
		{	
			OutboundCall outboundCall=new OutboundCall();
		outboundCall.setBroadcastSchedule(broadcastsch);
		outboundCall.setBroadcastRecipient(broadcastrcpt);
		outboundCall.setKookoo_id(kookoo_id);
		outboundCall.setStatus(status);
		outboundCall.setStatusDetail(statusDetails);
		outboundCall.setDuration(Integer.parseInt(request.getParameter("duration"))); 
		outboundCallService.addOutboundCall(outboundCall);
		}
		}
		catch(NullPointerException e)
		{
			System.out.println("Null pointer exception occured in BroadcastCallHandlerController ");
		}
	}


}



