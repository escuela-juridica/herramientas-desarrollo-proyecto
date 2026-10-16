package pe.edu.escuela.app.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

class CursoFechaFormatoTest {

  @Test
  void fechasDelFormularioSeLeenYSeMuestranEnFormatoHtmlSinDependerDelIdioma() {
    LocaleContextHolder.setLocale(Locale.forLanguageTag("es-PE"));
    try {
      Curso curso = new Curso();
      DataBinder binder = new DataBinder(curso);
      binder.setConversionService(new DefaultFormattingConversionService());
      binder.bind(new MutablePropertyValues(Map.of(
          "fechaInicio", "2026-10-13",
          "fechaFin", "2026-12-13")));

      assertThat(binder.getBindingResult().hasErrors()).isFalse();
      assertThat(curso.getFechaInicio()).isEqualTo(LocalDate.of(2026, 10, 13));
      assertThat(curso.getFechaFin()).isEqualTo(LocalDate.of(2026, 12, 13));
      assertThat(binder.getBindingResult().getFieldValue("fechaInicio"))
          .isEqualTo("2026-10-13");
      assertThat(binder.getBindingResult().getFieldValue("fechaFin"))
          .isEqualTo("2026-12-13");
    } finally {
      LocaleContextHolder.resetLocaleContext();
    }
  }
}
