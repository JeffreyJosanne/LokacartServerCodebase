package app.telephony.fsm.action.member;

import in.ac.iitb.ivrs.telephony.base.IVRSession;

import java.util.Arrays;

import app.telephony.RuralictSession;
import app.telephony.config.Configs;
import app.telephony.fsm.RuralictStateMachine;

import com.continuent.tungsten.commons.patterns.fsm.Action;
import com.continuent.tungsten.commons.patterns.fsm.Event;
import com.continuent.tungsten.commons.patterns.fsm.Transition;
import com.continuent.tungsten.commons.patterns.fsm.TransitionFailureException;
import com.continuent.tungsten.commons.patterns.fsm.TransitionRollbackException;
import com.ozonetel.kookoo.CollectDtmf;
import com.ozonetel.kookoo.Response;

public class AskForLanguageAction implements Action<IVRSession> {

	@Override
	public void doAction(Event<?> event, IVRSession session, Transition<IVRSession, ?> transition, int actionType)
			throws TransitionRollbackException, TransitionFailureException {
		RuralictSession ruralictSession = (RuralictSession) session;

		Response response = session.getResponse();
		CollectDtmf cd = new CollectDtmf();
		int i=1,j=0;
		String[] responses = new String[RuralictStateMachine.tempLanguageMap.size()];
		String[] responseKeys=new String[RuralictStateMachine.tempLanguageMap.size()];
		for(String key:RuralictStateMachine.tempLanguageMap.keySet()){
			responseKeys[j]=key;
			j++;
		}
		Arrays.sort(responseKeys);


		for(String k:responseKeys){
			String language = RuralictStateMachine.tempLanguageMap.get(k);

			if(language==null || language.equalsIgnoreCase(ruralictSession.getLanguage())){
				continue;
			}
			if(language.equalsIgnoreCase("en"))
			{		
				response.addPlayAudio(Configs.Voice.VOICE_DIR+"/for_"+language+".mp3"); //For
				response.addPlayAudio(Configs.Voice.VOICE_DIR+"/press_"+(i)+"_"+language+".mp3"); //Press
			}
			else
			{

				response.addPlayAudio(Configs.Voice.VOICE_DIR+"/for_"+language+".mp3"); //For
				response.addPlayAudio(Configs.Voice.VOICE_DIR+"/press_"+(i)+"_"+language+".mp3"); //Press

			}
			responses[i-1]=language;
			i++;
		}
		RuralictStateMachine.tempLanguageMap.clear();
		for(i=0;i<responses.length;i++){
			RuralictStateMachine.tempLanguageMap.put(((Integer)(i+1)).toString(), responses[i]);
		}

		cd.setMaxDigits(1);
		cd.setTimeOut(Configs.Telephony.DTMF_TIMEOUT);
		response.addCollectDtmf(cd);
	}

}
