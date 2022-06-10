package com.matyrobbrt.dataobjects.api.script;

import org.jetbrains.annotations.Nullable;

/**
 * A runner that will be used for running scripts. <br>
 * A runner may be registered either using {@link com.matyrobbrt.dataobjects.api.DataObjectsAPI.Scripts#registerRunner(ScriptRunner)}
 * or by providing a {@link java.util.ServiceLoader Service Loader} registry.
 */
public interface ScriptRunner {

    /**
     * Gets the extension of the scripts this runner can run. <br>
     * Example: js, groovy, java, kts, etc
     *
     * @return the extension of the scripts this runner can run
     */
    String extension();

    /**
     * Runs a script.
     *
     * @param context the context
     * @return the value returned by the script execution. Maybe be {@code null}.
     */
    @Nullable
    Object run(ScriptContext context);
}
