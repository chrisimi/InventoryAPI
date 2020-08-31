# InventoryAPI
A small inventory API for Spigot > 1.13

With this small API you can create chat events and inventory clicks.

[example] https://github.com/chrisimi/InventoryAPI/blob/master/src/com/chrisimi/inventoryapi/sample/TestInventory.java

## download 
clone the latest build from github `git clone https://www.github.com/chrisimi/InventoryAPI.git`. Then you have to update the project so that maven can download the dependencies and afterwards you use `maven clean install` to install the library in your local maven repository to use it

## integration with maven
When you are finished with the first step then you have to integrate it in your plugin.

Maven:
```
<dependency>
  <groupId>com.chrisimi</groupId>
  <artifactId>InventoryAPI</artifactId>
  <version>1.0</version>
</dependency>
```

You also need the apache maven shade plugin to put the library into the jar otherwise class not found exceptions will happen:

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.1.0</version>
  <configuration>
    <createDependencyReducedPom>false</createDependencyReducedPom>
    <artifactSet>
      <includes>
        <include>com.chrisimi:InventoryAPI</include>
      </includes>
    </artifactSet>
  </configuration>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <outputFile>${output-directory}</outputFile>
      </configuration>
    </execution>
  </executions>
</plugin>

```

# Bugs
If you have found a bug write a issue with detailed information
