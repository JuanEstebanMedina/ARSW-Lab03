package edu.eci.arsw.threads;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListThread extends Thread {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(BlackListThread.class.getName());
    private List<Integer> blackListOcurrences = new LinkedList<>();

    // private int ocurrencesCount;
    private int checkedListsCount;
    private String ipaddress;
    private int start;
    private int end;

    private final Object syncObject;
    private final int[] sharedOccurrencesCount;
    private boolean shouldStop;

    public BlackListThread(int start, int end, String ipaddress, Object syncObject, int[] sharedOccurrencesCount) {
        this.ipaddress = ipaddress;
        // this.ocurrencesCount = 0;
        this.start = start;
        this.end = end;
        this.syncObject = syncObject;
        this.sharedOccurrencesCount = sharedOccurrencesCount;
        this.shouldStop = false;
        this.checkedListsCount = 0;
    }

    // public int getOcurrencesCount() {
    //     return ocurrencesCount;
    // }

    // public void incrementOcurrencesCount() {
    //     this.ocurrencesCount++;
    // }

    public List<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }

    @Override
    public void run() {
        System.out.println("\nBlackListThread is running...");
        checkHost(start, end, ipaddress);
    }

    public void checkHost(int start, int end, String ipaddress) {

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        checkedListsCount = 0;

        for (int i = start; i <= end; i++) {
            if (shouldStop){
                break;
            }

            synchronized (syncObject){
                if (sharedOccurrencesCount[0] >= BLACK_LIST_ALARM_COUNT){
                    break;
                }
            }

            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)) {

                blackListOcurrences.add(i);

                // incrementOcurrencesCount();
                synchronized (syncObject) {
                    sharedOccurrencesCount[0]++;
                }
            }
        }

        LOG.log(Level.INFO, "Thread start: {0} end: {1}, Checked Black Lists:{2} of {3}",
                new Object[] { start, end, checkedListsCount, end + 1 - start });

    }

    public void stopSearching() {
        this.shouldStop = true;
    }

}
