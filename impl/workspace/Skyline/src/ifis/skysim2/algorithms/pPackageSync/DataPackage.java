/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2.algorithms.pPackageSync;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Christoph
 */
public class DataPackage {

    private final Queue<float[]> queue = new LinkedList<float[]>();

    /**
     * @return the queue
     */
    public Queue<float[]> getQueue() {
        return queue;
    }
  
}
