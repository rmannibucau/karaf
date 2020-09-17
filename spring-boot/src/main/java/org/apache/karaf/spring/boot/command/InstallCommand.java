package org.apache.karaf.spring.boot.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.spring.boot.SpringBootService;

import java.net.URI;

@Service
@Command(scope = "spring-boot", name = "install", description = "Install a spring-boot fatjar")
public class InstallCommand implements Action {
    @Reference
    private SpringBootService springBootService;

    @Argument(name = "location", description = "The fatjar location", required = true)
    private String location;

    @Option(name = "stack", description = "The stack id to use for that app", required =  false)
    private String stack;

    @Override
    public Object execute() throws Exception {
        springBootService.install(URI.create(location), stack);
        return "ok";
    }
}
