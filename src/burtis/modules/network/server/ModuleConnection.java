package burtis.modules.network.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import burtis.modules.network.Listener;
import burtis.modules.network.ListenerImpl;
import burtis.modules.network.SocketService;

/**
 * Manages connection on given port. Awaits for incoming connection. After it's
 * setup, a new {@link ListenerImpl} is created and run for it.
 * 
 * Provides list of receivers to whom {@link ListenerImpl} will forward incoming
 * communication.
 * 
 * @author Amadeusz Sadowski
 *
 */
public class ModuleConnection
{
    private final ExecutorService connectingExecutor = Executors
            .newSingleThreadExecutor();
    private ExecutorService listenerExecutor = Executors
            .newSingleThreadExecutor();
    private final Listener listener;
    private final static Logger logger = Logger.getLogger(Server.class
            .getName());
    private final String moduleName;
    private final List<ModuleConnection> recipients = new ArrayList<ModuleConnection>();
    private final SocketService socketService;

    public ModuleConnection(final String moduleName,
            final SocketService socketService, final Consumer<Object> receive)
    {
        this.moduleName = moduleName;
        this.socketService = socketService;
        this.listener = new ListenerImpl(socketService, receive, this::connect,
                logger);
    }

    public void addRecipient(final ModuleConnection module)
    {
        recipients.add(module);
    }

    public void close()
    {
        listenerExecutor.shutdownNow();
        socketService.close();
    }

    /**
     * Attempts to open connection on separate thread. Non-blocking. Results in
     * RuntimeException on failure.
     */
    public void connect()
    {
        connectingExecutor.execute(this::tryConnect);
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public SocketService getSocketService()
    {
        return socketService;
    }

    private void tryConnect()
    {
        try
        {
            logger.log(Level.INFO, String.format(
                    "Oczekuję na połączenie z modułem %1$s na porcie %2$d",
                    moduleName, socketService.getPort()));
            listenerExecutor.shutdownNow();
            listenerExecutor = Executors.newSingleThreadExecutor();
            socketService.connect();
            listenerExecutor.execute(listener::listen);
            logger.log(Level.INFO, String.format(
                    "Podłączył się klient '%1$s' na porcie %2$d", moduleName,
                    socketService.getPort()));
        }
        catch (final IOException e)
        {
            logger.log(Level.SEVERE, "Błąd tworzenia Listener'a", e);
            throw new RuntimeException(e);
        }
    }
}
