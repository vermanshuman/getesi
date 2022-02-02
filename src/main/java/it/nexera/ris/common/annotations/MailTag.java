package it.nexera.ris.common.annotations;

import it.nexera.ris.common.enums.EmailPDFTags;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MailTag {
    EmailPDFTags emailTag();
}
