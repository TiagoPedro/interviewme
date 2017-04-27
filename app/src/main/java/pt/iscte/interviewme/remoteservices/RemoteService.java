package pt.iscte.interviewme.remoteservices;

import android.app.Activity;
import android.app.Service;

/**
 * Created by tiago on 27/02/2017.
 */

public interface RemoteService
{
    // Variables
//    private boolean isActive;

    // Abstract methods
    void start();
    void stop();

    // General methods
    boolean getActiveStatus();

    void setActiveStatus(boolean status);
}
