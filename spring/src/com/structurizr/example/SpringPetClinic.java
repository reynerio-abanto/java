package com.structurizr.example;

import com.structurizr.componentfinder.ComponentFinder;
import com.structurizr.componentfinder.SpringComponentFinderStrategy;
import com.structurizr.io.json.JsonWriter;
import com.structurizr.model.*;
import com.structurizr.view.ViewSet;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ContainerView;
import com.structurizr.view.SystemContextView;

import java.io.StringWriter;

/**
 * This is a C4 representation of the Spring PetClinic sample app (https://github.com/spring-projects/spring-petclinic/).
 */
public class SpringPetClinic {

    public static void main(String[] args) throws Exception {
        Model model = new Model("Spring PetClinic", "This is a C4 representation of the Spring PetClinic sample app (https://github.com/spring-projects/spring-petclinic/)");

        // create the basic model (the stuff we can't get from the code)
        SoftwareSystem springPetClinic = model.addSoftwareSystem(Location.Internal, "Spring PetClinic", "");
        Person user = model.addPerson(Location.External, "User", "");
        user.uses(springPetClinic, "Uses");

        Container webApplication = springPetClinic.addContainer("Web Application", "", "Apache Tomcat 7.x");
        Container relationalDatabase = springPetClinic.addContainer("Relational Database", "", "HSQLDB");
        user.uses(webApplication, "Uses");
        webApplication.uses(relationalDatabase, "Reads from and writes to");

        // and now automatically find all Spring @Controller, @Component, @Service and @Repository components
        ComponentFinder componentFinder = new ComponentFinder(webApplication, "org.springframework.samples.petclinic",
                new SpringComponentFinderStrategy());
        componentFinder.findComponents();

        // connect the user to all of the Spring MVC controllers
        webApplication.getComponents().stream().filter(c -> c.getTechnology().equals("Spring Controller")).forEach(c -> user.uses(c, "Uses"));

        // connect all of the repository components to the relational database
        webApplication.getComponents().stream().filter(c -> c.getTechnology().equals("Spring Repository")).forEach(c -> c.uses(relationalDatabase, "Reads from and writes to"));

        // finally create some views
        ViewSet viewSet = new ViewSet(model);
        SystemContextView contextView = viewSet.createContextView(springPetClinic);
        contextView.addAllSoftwareSystems();
        contextView.addAllPeople();

        ContainerView containerView = viewSet.createContainerView(springPetClinic);
        containerView.addAllPeople();
        containerView.addAllSoftwareSystems();
        containerView.addAllContainers();

        ComponentView componentView = viewSet.createComponentView(springPetClinic, webApplication);
        componentView.addAllComponents();
        componentView.addAllPeople();
        componentView.add(relationalDatabase);

        JsonWriter jsonWriter = new JsonWriter(false);
        StringWriter stringWriter = new StringWriter();
        jsonWriter.write(viewSet, stringWriter);

        System.out.println(stringWriter.toString());
    }

}