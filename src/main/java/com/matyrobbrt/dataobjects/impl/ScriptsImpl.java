package com.matyrobbrt.dataobjects.impl;

import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.script.ScriptContext;
import com.matyrobbrt.dataobjects.api.script.ScriptRunner;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class ScriptsImpl implements DataObjectsAPI.Scripts {

    private final Map<String, ScriptRunner> runners = new HashMap<>();

    @Override
    public void registerRunner(ScriptRunner runner) {
        runners.put(runner.extension(), runner);
    }

    @Override
    public @Nullable ScriptRunner getRunner(String extension) {
        return runners.get(extension);
    }

    @Override
    public ScriptContext createContext(String script) {
        return new ContextImpl(script, new HashMap<>());
    }

    private record ContextImpl(String script, Map<String, Object> variables) implements ScriptContext {

        @Override
        public String getScript() {
            return script;
        }

        @Override
        public void addVariable(String name, Object var) {
            variables.put(name, var);
        }

        @Override
        public Map<String, Object> getVariables() {
            return variables;
        }
    }
}
