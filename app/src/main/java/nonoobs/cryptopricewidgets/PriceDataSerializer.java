package nonoobs.cryptopricewidgets;

import androidx.datastore.core.Serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import nonoobs.cryptopricewidgets.model.PriceData;

public class PriceDataSerializer implements Serializer<PriceData> {
    @Override
    public PriceData getDefaultValue() {
        return PriceData.newBuilder().build();
    }

    @Override
    public @Nullable Object readFrom(@NotNull InputStream inputStream, @NotNull Continuation<? super PriceData> continuation) {
        try {
            PriceData priceData = PriceData.parseFrom(inputStream);
            CryptoAppWidgetLogger.info(String.format("Deserialized price data: %f", priceData.getPrice()));
            return priceData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable Object writeTo(PriceData priceData, @NotNull OutputStream outputStream, @NotNull Continuation<? super Unit> continuation) {
        try {
            CryptoAppWidgetLogger.info(String.format("Serializing price data: %f", priceData.getPrice()));
            priceData.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
