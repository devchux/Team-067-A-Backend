package com.sdgcrm.application.views.signup;

import com.sdgcrm.application.data.entity.Role;
import com.sdgcrm.application.data.entity.RoleName;
import com.sdgcrm.application.data.entity.User;
import com.sdgcrm.application.data.service.UserService;
import com.sdgcrm.application.email.ScheduleEmailRequest;
import com.sdgcrm.application.repository.RoleRepository;
import com.sdgcrm.application.service.EmailService;
import com.sdgcrm.application.views.AppConst;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Route("signup")
@CssImport("./styles/shared-styles.css")
@Viewport(AppConst.VIEWPORT)
public class SignUpView extends VerticalLayout implements BeforeEnterObserver {

    private Checkbox allowMarketingBox;
    private PasswordField passwordField1;
    private PasswordField passwordField2;


    Binder<User> binder = new Binder<>();

    @Autowired
    UserService userservice;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    EmailService emailservice;

    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    /**
     * We use Spring to inject the backend into our view
     */
    public SignUpView(UserService userservice, RoleRepository roleRepository, PasswordEncoder encoder, EmailService emailservice) {

        this.userservice= userservice;
        this.roleRepository= roleRepository;
        this.encoder= encoder;
        this.emailservice=emailservice;
        /*
         * Create the components we'll need
         */

        H2 title = new H2("Signup form");


        TextField firstnameField = new TextField("First name");
        binder.forField(firstnameField).asRequired().bind(User::getFirstname,User::setFirstname);




        TextField lastnameField = new TextField("Last name");
        binder.forField(lastnameField).asRequired().bind(User::getLastname,User::setLastname);


        EmailField emailField = new EmailField("Email");
        binder.forField(emailField).asRequired().bind(User::getEmail,User::setEmail);


        TextField companyNameField = new TextField("Company Name");
        binder.forField(companyNameField).asRequired().bind(User::getCompanyName,User::setCompanyName);

        ComboBox<String> companyPositionField = new ComboBox<>("Company Position");
        binder.forField(companyPositionField).bind(User::getCompanyPosition,User::setCompanyPosition);
        companyPositionField.setAllowCustomValue(false);

        TextField locationField = new TextField("Location");
        binder.forField(locationField).asRequired().bind(User::getLocation,User::setLocation);

        TextField phoneField = new TextField("Phone");
        binder.forField(phoneField).asRequired("Please Enter Phone Number").withConverter(new StringToLongConverter("Invalid Phone Number")).bind(User::getPhone,User::setPhone);

        // This is a custom field we create to handle the field 'avatar' in our data. It
        // work just as any other field, e.g. the TextFields above. Instead of a String
        // value, it has an AvatarImage value.

        // We'll need these fields later on so let's store them as class variables
        allowMarketingBox = new Checkbox("Accept Terms and Conditions");
        allowMarketingBox.getStyle().set("padding-top", "10px");


        emailField.setVisible(true);

        passwordField1 = new PasswordField("Wanted password");
        passwordField2 = new PasswordField("Confirm password");

        binder.forField(passwordField2).bind(User::getPassword,User::setPassword);

        Span errorMessage = new Span();
        Span successMessage = new Span();
        Button submitButton = new Button("Sign Up");

        Button loginButton = new Button("Already Signed Up");
        Anchor signIn = new Anchor("/", "Already Signed Up? Login");
        signIn.addClassName("centered-content");

        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*
         * Build the visible layout
         */
        companyPositionField.setItems(userservice.getCompanyPosition());
        // Create a FormLayout with all our components. The FormLayout doesn't have any
        // logic (validation, etc.), but it allows us to configure Responsiveness from
        // Java code and its defaults looks nicer than just using a VerticalLayout.
        FormLayout formLayout = new FormLayout(title,successMessage, firstnameField, lastnameField, companyNameField, companyPositionField, passwordField1, passwordField2,
                 emailField, phoneField,locationField, allowMarketingBox, errorMessage, submitButton, signIn);

        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // Allow the form layout to be responsive. On device widths 0-490px we have one
        // column, then we have two. Field labels are always on top of the fields.
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        // These components take full width regardless if we use one column or two (it
        // just looks better that way)
        formLayout.setColspan(title, 2);
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(submitButton, 2);
        formLayout.setColspan(successMessage, 2);
        formLayout.setColspan(signIn, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");

        successMessage.getStyle().set("color", "var(--lumo-success-text-color)");
        successMessage.getStyle().set("padding", "15px 0");

        // Add the form to the page
        add(formLayout);






//        // Only ask for email address if the user wants marketing emails
//        allowMarketingBox.addValueChangeListener(e -> {
//
//            // show or hide depending on the checkbox
//            emailField.setVisible(allowMarketingBox.getValue());
//
//            // Additionally, remove the input if the user decides not to allow emails. This
//            // way any input that ends up hidden on the page won't end up in the bean when
//            // saved.
//            if (!allowMarketingBox.getValue()) {
//                emailField.setValue("");
//            }
//        });

        passwordField2.addValueChangeListener(e -> {
             enablePasswordValidation = true;
            });


        submitButton.addClickListener(e -> {

            // Creating user's account

            if ( binder.validate().hasErrors()) {
                Notification.show("Validation error count: "
                        + binder.validate().getFieldValidationStatuses().toString());

                errorMessage.setText("you have "+binder.validate().getValidationErrors().size() +" errors");
            }else{



                String strRoles = "user";
                Set<Role> roles = new HashSet<>();


                switch(strRoles) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                        roles.add(adminRole);

                        break;
                    case "pm":
                        Role pmRole = roleRepository.findByName(RoleName.ROLE_PM)
                                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                        roles.add(pmRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                        roles.add(userRole);
                }
                User user = new User();

                user.setFirstname(firstnameField.getValue());
                user.setLastname(lastnameField.getValue());
                user.setEmail(emailField.getValue());
                user.setPhone( Long.parseLong(phoneField.getValue()));
                user.setLocation(locationField.getValue());
                user.setCompanyName(companyNameField.getValue());
                user.setCompanyPosition(companyPositionField.getValue());
                user.setPassword( encoder.encode(passwordField2.getValue()));
                user.setRoles(roles);

                System.out.println(user.toString());
                userservice.store(user);

                successMessage.setText("<br/> Thank you %s, your registration was submitted! please check your email to confirm account "+ emailField.getValue() +" or contact support");

                String msg = String.format(
                        "Thank you %s, your registration was submitted! please check your email to confirm account",
                        emailField.getValue());


                ScheduleEmailRequest welcomemail= new ScheduleEmailRequest();
                welcomemail.setEmail(emailField.getValue());
                welcomemail.setSubject("Welcome to the Eazy Life");
                welcomemail.setName(firstnameField.getValue());

                welcomemail.setTemplate("welcome.html");

                emailservice.scheduleEmail(welcomemail);


                Notification.show(msg, 3000, Notification.Position.MIDDLE);
                init();

            }






        });




    }


    private void init() {
        binder.setBean(new User());
    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess() {
        Notification notification = Notification.show("Data saved, welcome ");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Here you'd typically redirect the user to another view
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = passwordField2.getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }


    /**
     * Custom validator class that extends the built-in email validator.
     * <p>
     * Ths validator checks if the field is visible before performing the
     * validation. This way, the validation is only performed when the user has told
     * us they want marketing emails.
     */
    public class VisibilityEmailValidator extends EmailValidator {

        public VisibilityEmailValidator(String errorMessage) {
            super(errorMessage);
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {

            if (!allowMarketingBox.getValue()) {
                // Component not visible, no validation
                return ValidationResult.ok();
            } else {
                // normal email validation
                return super.apply(value, context);
            }
        }
    }

}
