package panels;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Background panel to manage background tasks and thread management,
 * providing utilities for running background tasks and cleanup operations
 */
public class Background_panel {
    private ExecutorService executor;
    private boolean isRunning;
    
    /**
     * Constructor initializes the thread pool for background tasks
     */
    public Background_panel() {
        this.executor = Executors.newCachedThreadPool();
        this.isRunning = true;
    }
    
    /**
     * Executes a task in the background
     */
    public void executeTask(Runnable task) {
        if (isRunning && !executor.isShutdown()) {
            executor.submit(task);
        }
    }
    
    /**
     * Shuts down all background tasks gracefully when application closes
     */
    public void shutdown() {
        if (isRunning && !executor.isShutdown()) {
            isRunning = false;
            
            // First attempt a graceful shutdown
            executor.shutdown();
            
            try {
                // Wait for tasks to complete
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks don't complete in time
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Re-interrupt the thread
                Thread.currentThread().interrupt();
                // Force shutdown
                executor.shutdownNow();
            }
        }
    }
    
    /**
     * Checks if background tasks are still running
     */
    public boolean isRunning() {
        return isRunning && !executor.isShutdown();
    }
    
    /**
     * Creates a new executor if the current one is shut down when restarting application
     */
    public void restart() {
        if (!isRunning || executor.isShutdown()) {
            executor = Executors.newCachedThreadPool();
            isRunning = true;
        }
    }
}
