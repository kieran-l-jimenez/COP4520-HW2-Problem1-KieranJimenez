/* Problem 1: Minotaur's Birthday Party
 * We have N guests (N threads, let N = 5)
 * We have one shared cupcake plate (Some shared structure like Counter that flips between present and not, 1/0)
 *  and infinite cupcakes (no limit to number of flips)
 *  one guest at a time (lock on counter)
 * Minotaur may pick same guest more than once (Minotaur = multi-thread policy)
 * Guests can't communicate (no direct messages between threads or master thread they update)
 * Must show that everyone has had a cupcake at least once
 *
 * Solution Ideas:
 * 1. Each thread tracks the number of times they enter the room
 *  Say finished when someone has visited the room N times
 *      ISSUE: Only works if minotaur is fair
 * 2. Each thread tracks whether they've visited. If they haven't, they flip the cake flag, else they leave it
 *  If they see the same state N times in a row they say everyone visited
 *      ISSUE: Same as above, minotaur can just call same guest N times in a row
 * THIS ONE 3. One thread in charge of calling for new cupcakes, counts number of cupcakes he has ordered
 *  Normie thread eats if they haven't, leaves it alone if they have eaten
 *  Once Leader thread has ordered N-2 cupcakes (sorry leader, no cupcake for you), tell minotaur everyone has entered
 */
public class ProblemOne {
    static int numGuests = 5;

    public static void main(String[] args) throws InterruptedException {
        guestThread[] allGuests = new guestThread[numGuests];//1 leader thread and 4 regular guest threads
        allGuests[0] = new leaderThread();
        for (int i = 1; i < allGuests.length; i++) {
            allGuests[i] = new guestThread();
        }
        for (Thread thread : allGuests) {//start all threads
            thread.start();
        }
        allGuests[0].join();//wait for the leader to say everyone has eaten
        for (guestThread thread : allGuests) {//start all threads
            thread.closeDown();
        }
        System.out.println("Every guest has visited the labyrinth");
    }

    public static class cakeRoom {
        static boolean cakePresent = true;

        synchronized public static boolean enterRoom(int behaviorType) {//1: leader, 2: hungry guest, 3: sated guest
            switch (behaviorType) {
                case 1:
                    if (!cakePresent) {//no cake present
                        cakePresent = true;//replace it
                        return true;//remember that you replaced it
                    }
                    return false;//remember that you didn't replace it this time
                case 2:
                    if (cakePresent) {//cake is present
                        cakePresent = false;//eat it
                        return true;//remember eating it
                    }
                    return false;//remember that you're still hungry
                case 3:
                    return true;//don't eat it and remain content
            }
        return false;//WHO ARE YOU?
        }
    }

    public static class guestThread extends Thread {
        static boolean exitFlag = false;
        static boolean hungry = true;
        public void run() {
            while (!exitFlag) {
                if (hungry) {//if hungry...
                    if(cakeRoom.enterRoom(2)) {//...enter as a hungry guest...
                        hungry = false;//...and if ate cupcake, no longer hungry
                    }
                } else {
                    hungry = !cakeRoom.enterRoom(3);//keep visiting as minotaur wants, but don't do anything
                }
            }
        }
        public void closeDown() {
            exitFlag = true;
        }
    }

    public static class leaderThread extends guestThread {
        static int numCakesOrdered = 1;//we know that we start with one cupcake in the labyrinth
        @Override
        public void run() {
            while (!exitFlag) {//while minotaur accepts guests
                if (cakeRoom.enterRoom(1)) {//enter room as leader and if we called for a new cupcake...
                    numCakesOrdered++;//...note down we have seen more cupcakes
                }
                if (numCakesOrdered >= numGuests) {
                    /*if there have been as many cupcakes as guests and everyone eats only one, by counting we know
                    every guest has visited the labyrinth*/
                    this.closeDown();
                }
            }
        }
    }
}
