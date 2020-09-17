package org.apache.karaf.spring.boot.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.spring.boot.SpringBootService;

@Service
@Command(scope = "spring-boot", name = "stop", description = "Start an already installed spring-boot fatjar")
public class StopCommand implements Action {
    @Reference
    private SpringBootService springBootService;

    @Argument(name = "jarname", description = "The fatjar name", required = true)
    private String name;

    @Override
    public Object execute() {
        springBootService.stop(name);
        return "ok";
    }
}
