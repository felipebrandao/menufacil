package br.com.felipebrandao.menufacil.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BuildInfoContributor implements InfoContributor {

    private final BuildProperties buildProperties;

    public BuildInfoContributor(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> app = new LinkedHashMap<>();

        if (buildProperties != null) {
            app.put("name", buildProperties.getName());
            app.put("version", buildProperties.getVersion());
            app.put("time", buildProperties.getTime());
        }

        builder.withDetail("app", app);

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("javaVersion", System.getProperty("java.version"));
        runtime.put("timezone", System.getProperty("user.timezone"));
        builder.withDetail("runtime", runtime);
    }
}

