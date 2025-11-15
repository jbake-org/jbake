package org.jbake.util;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.Property;

import java.io.PrintStream;
import java.util.List;

public class ConfigurationPrinter {

    private PrintStream out;
    private JBakeConfiguration configuration;

    public ConfigurationPrinter(JBakeConfiguration configuration, PrintStream out) {
        this.out = out;
        this.configuration = configuration;
    }

    public void print() {

        List<Property> properties = configuration.getJbakeProperties();
        Property.Group lastGroup = null;

        for (Property property : properties) {

            if (lastGroup != property.getGroup()) {
                printGroup(property);
                this.printHeader();
            }
            if (!property.getDescription().isEmpty()) {
                printDescription(property);
            }
            printKeyAndValue(property);

            lastGroup = property.getGroup();
        }
    }

    private void printHeader() {
        out.printf("%1$-40s %2$-40s%n", "Key", "Value");
        out.println(getHorizontalLine());
    }

    private String getHorizontalLine() {
        return String.format("%080d%n", 0).replace("0", "-");
    }

    private void printGroup(Property property) {
        out.printf("%n%s - Settings%n%n", property.getGroup());
    }

    private void printDescription(Property property) {
        out.printf("# %s%n", property.getDescription());
    }

    private void printKeyAndValue(Property property) {
        String key = leftFillWithDots(property.getKey());
        Object value = configuration.get(property.getKey());
        out.printf("%1$s: %2$-40s%n%n", key, value);
    }

    private String leftFillWithDots(String value) {
        return String.format("%1$-40s", value).replace(' ', '.');
    }

}
