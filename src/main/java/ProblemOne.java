/* Problem 1: Minotaur's Birthday Party
 * We have N guests (N threads, let N = 45)
 * We have one shared cupcake plate (Some shared structure like Counter that flips between present and not, 1/0)
 *  and infinite cupcakes (no limit to number of flips)
 *  one guest at a time (lock on counter)
 * Minotaur may pick same guest more than once (Minotaur = multithread policy)
 * Guests can't communicate (no direct messages between threads or master thread they update)
 * Must show that everyone has had a cupcake at least once
 *
 * Solution Ideas:
 * 1. Each thread tracks the number of times they enter the room
 *  Say finished when someone has visited the room N times
 *      ISSUE: Only works if minotaur is fair
 * 2. Each thread tracks whether they've visited. If they haven't, they flip the cakeflag, else they leave it
 *  If they see the same state N times in a row they say everyone visited
 *      ISSUE: Same as above, minotaur can just call same guest N times in a row
 * 3. One thread in charge of calling for new cupcakes, counts number of cupcakes he has ordered
 *  Normie thread eats if they haven't, leaves it alone if they have eaten
 *  Once Leader thread has ordered N-2 cupcakes (sorry leader, no cupcake for you)
 */
public class ProblemOne {
    static boolean cakeFlag = true;
    static int numGuests = 45;

    public static void main(String[] args) throws InterruptedException {
        Thread[] allGuests = new Thread[numGuests];//1 leader thread and 44 regular guest threads
        allGuests[0] = new leaderThread();
        for (int i = 1; i < allGuests.length; i++) {
            allGuests[i] = new guestThread();
        }
        for (Thread thread : allGuests) {//start all threads
            thread.start();
        }
        allGuests[0].notify();//tell leader to stop waiting and start tracking cake status
        allGuests.notifyAll();//tell minotaur he can start inviting guests
        allGuests[0].join();//wait for the leader to say everyone has eaten
        System.out.println("Every guest has visited the labyrinth");
    }

    public static class guestThread extends Thread {
        static boolean hungry = true;
        public void run() {
            try {
                wait();
                while (true) {
                    if (hungry && cakeFlag) {//if guest hungry and sees cake, will eat it
                        synchronized (this) {
                            hungry = false;
                            cakeFlag = false;
                        }
                    }
                    //if the guest isn't hungry or doesn't see cake, it doesn't change the cakeFlag
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class leaderThread extends Thread {
        static int numCakesOrdered = 0;
        public void run() {
            try {
                wait();
                while (true) {
                    if (!cakeFlag) {
                        cakeFlag = true;
                        numCakesOrdered++;
                    }
                    if (numCakesOrdered > numGuests) {
                        break;//ends and returns to main
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
