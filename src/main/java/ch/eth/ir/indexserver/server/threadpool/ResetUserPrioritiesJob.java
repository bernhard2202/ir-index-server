package ch.eth.ir.indexserver.server.threadpool;

import java.util.TimerTask;

import ch.eth.ir.indexserver.server.security.UserProperties;

public class ResetUserPrioritiesJob extends TimerTask{

   @Override
   public void run() {
     UserProperties.resetRequestCounts();
   }

}