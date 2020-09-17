package org.apache.karaf.spring.boot.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
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

    @Override
    public Object execute() throws Exception {
        springBootService.install(URI.create(location));
        return "ok";
    }
}
