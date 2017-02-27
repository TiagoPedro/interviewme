package pt.iscte.interviewme.remoteservices;

/**
 * Created by tiago on 27/02/2017.
 */

abstract class RemoteService
{
    // Variables
    private boolean isActive;

    // Abstract methods
    abstract boolean startService();
    abstract boolean stopService();

    // General methods
    boolean getActiveStatus()
    {
        return isActive;
    }

    void setActiveStatus(boolean status)
    {
        isActive = status;
    }
}
