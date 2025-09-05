/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.threads.BlackListThread;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * 
     * @param ipaddress suspicious host's IP address.
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress) {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        int ocurrencesCount = 0;

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount = 0;

        for (int i = 0; i < skds.getRegisteredServersCount() && ocurrencesCount < BLACK_LIST_ALARM_COUNT; i++) {
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)) {

                blackListOcurrences.add(i);

                ocurrencesCount++;
            }
        }

        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}",
                new Object[] { checkedListsCount, skds.getRegisteredServersCount() });

        return blackListOcurrences;
    }

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * 
     * @param ipaddress   suspicious host's IP address.
     * @param threadCount number of threads to be used.
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int threadCount) {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        List<BlackListThread> blackListThreadList = new LinkedList<>();

        // int ocurrencesCount = 0;

        Object syncObject = new Object();
        int[] sharedOccurrencesCount = {0};

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount = 0;

        int range = skds.getRegisteredServersCount() / threadCount; // 80000 / 3 = 26666
        // thread 1 -> 0 - 26665
        // thread 2 -> 26666 - 53331
        // thread 3 -> 53332 - 79999

        for (int i = 0; i < threadCount; i++) {
            int start = i * range;
            int end = (i == threadCount - 1) ? skds.getRegisteredServersCount() - 1 : (i + 1) * range - 1;
            BlackListThread thread = new BlackListThread(start, end, ipaddress, syncObject, sharedOccurrencesCount);
            blackListThreadList.add(thread);
            thread.start();
        }

        boolean limitReached = false;
        while (!limitReached) {
            synchronized (syncObject) {
                if (sharedOccurrencesCount[0] >= BLACK_LIST_ALARM_COUNT) {
                    limitReached = true;
                    // Indicar a todos los hilos que deben detenerse
                    for (BlackListThread thread : blackListThreadList) {
                        thread.stopSearching();
                    }
                }
            }

            
        }

        for (BlackListThread thread : blackListThreadList) {
            try {
                thread.join();
                // ocurrencesCount += thread.getOcurrencesCount();
                checkedListsCount += thread.getCheckedListsCount();
                blackListOcurrences.addAll(thread.getBlackListOcurrences());
                System.out.println("thread.getBlackListOcurrences() " + thread.getBlackListOcurrences());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.SEVERE, "Thread interrupted", e);
            }
        }

        int finalOccurrencesCount;
        synchronized (syncObject) {
            finalOccurrencesCount = sharedOccurrencesCount[0];
        }

        if (finalOccurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}",
                new Object[] { checkedListsCount, skds.getRegisteredServersCount() });

        return blackListOcurrences;
    }
}
