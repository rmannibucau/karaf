package org.apache.karaf.spring.boot.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.spring.boot.SpringBootService;

import java.net.URI;
import java.util.List;

@Service
@Command(scope = "spring-boot", name = "start", description = "Start an already installed spring-boot fatjar")
public class StartCommand implements Action {
    @Reference
    private SpringBootService springBootService;

    @Argument(name = "jarname", description = "The fatjar name followed by the main args", required = true, multiValued = true)
    private List<String> args;

    @Override
    public Object execute() throws Exception {
        if (args == null || args.isEmpty()) {
            throw new IllegalArgumentException("Missing jar name");
        }
        springBootService.start(args.remove(0), args.toArray(new String[0]));
        return "ok";
    }
}
