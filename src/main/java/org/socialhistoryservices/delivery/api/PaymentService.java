package org.socialhistoryservices.delivery.api;

import com.mollie.mollie.Client;
import com.mollie.mollie.models.components.Amount;
import com.mollie.mollie.models.components.Locale;
import com.mollie.mollie.models.components.PaymentLineType;
import com.mollie.mollie.models.components.PaymentRequest;
import com.mollie.mollie.models.components.PaymentRequestLines;
import com.mollie.mollie.models.components.PaymentResponse;
import com.mollie.mollie.models.components.Security;
import com.mollie.mollie.models.components.SequenceType;
import com.mollie.mollie.models.operations.CreatePaymentResponse;
import com.mollie.mollie.models.operations.GetPaymentRequest;
import com.mollie.mollie.models.operations.GetPaymentResponse;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentService {
    private final String baseUrl;
    private final Client client;

    public PaymentService(String baseUrl, String apiKey, String profileId) {
        this.baseUrl = baseUrl;
        this.client = Client.builder()
                .profileId(profileId)
                .security(Security.builder().apiKey(apiKey).build())
                .build();
    }

    public PaymentResponse createPaymentForReproduction(Reproduction r) throws PaymentException {
        List<PaymentRequestLines> lines = r.getHoldingReproductions().stream().map(hr ->
                PaymentRequestLines.builder()
                        .type(PaymentLineType.DIGITAL)
                        .description(hr.toShortString())
                        .quantity(1)
                        .unitPrice(getAmountFor(hr.getCompletePrice()))
                        .discountAmount(getAmountFor(hr.getDiscount()))
                        .vatRate(hr.getBtwPercentage().toString())
                        .vatAmount(getAmountFor(hr.getBtwPrice()))
                        .totalAmount(getAmountFor(hr.getCompletePriceWithDiscount()))
                        .build()
        ).collect(Collectors.toList());

        if (r.getAdminstrationCosts().compareTo(BigDecimal.ZERO) > 0) {
            lines.add(PaymentRequestLines.builder()
                    .type(PaymentLineType.SURCHARGE)
                    .description("Administration costs")
                    .quantity(1)
                    .unitPrice(getAmountFor(r.getAdminstrationCosts()))
                    .discountAmount(getAmountFor(r.getAdminstrationCostsDiscount()))
                    .vatRate(r.getAdminstrationCostsBtwPercentage().toString())
                    .vatAmount(getAmountFor(r.getAdminstrationCostsBtwPrice()))
                    .totalAmount(getAmountFor(r.getAdminstrationCostsWithDiscount()))
                    .build());
        }

        CreatePaymentResponse response = client.payments().create().paymentRequest(PaymentRequest.builder()
                        .description("IISH reproduction " + r.getId())
                        .amount(getAmountFor(r.getTotalPriceWithDiscount()))
                        .sequenceType(SequenceType.ONEOFF)
                        .digitalGoods(true)
                        .redirectUrl(this.baseUrl + "/reproduction/order/redirect")
                        .cancelUrl(this.baseUrl + "/reproduction/order/cancel")
                        .webhookUrl(this.baseUrl + "/reproduction/order/webhook")
                        .locale(Locale.fromValue(r.getRequestLocale().toString()).orElse(Locale.EN_US))
                        .lines(lines)
                        .build())
                .call();

        return response.paymentResponse().orElseThrow(() -> new PaymentException(response));
    }

    public PaymentResponse getPaymentDetails(String paymentId) throws PaymentException {
        GetPaymentRequest paymentRequest = GetPaymentRequest.builder().paymentId(paymentId).build();
        GetPaymentResponse response = client.payments().get().request(paymentRequest).call();
        return response.paymentResponse().orElseThrow(() -> new PaymentException(response));
    }

    private static Amount getAmountFor(BigDecimal amount) {
        return Amount.builder()
                .currency("EUR")
                .value(amount.toString())
                .build();
    }
}
