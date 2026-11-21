package tn.tekup.educated;

import org.springframework.boot.SpringApplication;

public class TestEducatedBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(EducatedBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
