package org.entur.kingu.netex.mapping;

import org.entur.netex2.model.MultilingualString;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.TextType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetexMultilingualStringHelperTest {

    @Test
    void writesValueAsInlineText() {
        MultilingualString multilingualString = NetexMultilingualStringHelper.toNetexModel("Oslo S", "nor");

        assertThat(multilingualString.getContent()).containsExactly("Oslo S");
        assertThat(multilingualString.getLang()).isEqualTo("nor");
    }

    @Test
    void writesNullForNullValue() {
        assertThat(NetexMultilingualStringHelper.toNetexModel(null, "nor")).isNull();
    }

    @Test
    void readsInlineText() {
        MultilingualString multilingualString = new MultilingualString().withContent("Oslo S").withLang("nor");

        assertThat(NetexMultilingualStringHelper.getValue(multilingualString)).isEqualTo("Oslo S");
        assertThat(NetexMultilingualStringHelper.getLang(multilingualString)).isEqualTo("nor");
    }

    @Test
    void readsTextElement() {
        TextType text = new TextType().withValue("Oslo S").withLang("nor");
        MultilingualString multilingualString = new MultilingualString()
                .withContent(new ObjectFactory().createMultilingualStringText(text));

        assertThat(NetexMultilingualStringHelper.getValue(multilingualString)).isEqualTo("Oslo S");
        assertThat(NetexMultilingualStringHelper.getLang(multilingualString)).isEqualTo("nor");
    }

    @Test
    void roundTripsValueAndLang() {
        MultilingualString multilingualString = NetexMultilingualStringHelper.toNetexModel("Oslo S", "nor");

        assertThat(NetexMultilingualStringHelper.getValue(multilingualString)).isEqualTo("Oslo S");
        assertThat(NetexMultilingualStringHelper.getLang(multilingualString)).isEqualTo("nor");
    }
}
