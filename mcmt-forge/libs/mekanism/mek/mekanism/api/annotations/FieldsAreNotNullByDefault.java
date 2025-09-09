package mekanism.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import org.jetbrains.annotations.NotNull;

@Nonnull
@TypeQualifierDefault({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
@NotNull
public @interface FieldsAreNotNullByDefault {
}
