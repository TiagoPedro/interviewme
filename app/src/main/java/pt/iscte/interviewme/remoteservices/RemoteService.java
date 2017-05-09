package pt.iscte.interviewme.remoteservices;


public interface RemoteService
{
    void start();
    void stop();

    boolean getActiveStatus();
    void setActiveStatus(boolean status);
}
