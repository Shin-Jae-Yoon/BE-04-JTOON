package shop.jtoon.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import shop.jtoon.dto.CancelDto;
import shop.jtoon.exception.IamportException;
import shop.jtoon.exception.InvalidRequestException;
import shop.jtoon.type.ErrorStatus;

@Service
@Transactional(readOnly = true)
public class IamportService {

	private final IamportClient iamportClient;

	public IamportService(
		@Value("${pg.kg-inicis.rest-api-key}") String restApiKey,
		@Value("${pg.kg-inicis.rest-api-secret}") String restSecretKey
	) {
		this.iamportClient = new IamportClient(restApiKey, restSecretKey);
	}

	public void validateIamport(String impUid, BigDecimal amount) {
		try {
			IamportResponse<Payment> irsp = iamportClient.paymentByImpUid(impUid);
			BigDecimal realAmount = irsp.getResponse().getAmount();
			validateAmount(realAmount, amount);
		} catch (IamportResponseException | IOException e) {
			throw new IamportException(e.getMessage());
		}
	}

	@Transactional
	public void cancelIamport(CancelDto cancelDto) {
		try {
			IamportResponse<Payment> irsp = iamportClient.paymentByImpUid(cancelDto.impUid());
			CancelData cancelData = new CancelData(irsp.getResponse().getImpUid(), true);
			cancelData.setReason(cancelDto.reason());
			cancelData.setChecksum(cancelDto.checksum());
			cancelData.setRefund_holder(cancelDto.refundHolder());
			iamportClient.cancelPaymentByImpUid(cancelData);
		} catch (IamportResponseException | IOException e) {
			throw new IamportException(e.getMessage());
		}
	}

	private void validateAmount(BigDecimal realAmount, BigDecimal amount) {
		if (!realAmount.equals(amount)) {
			throw new InvalidRequestException(ErrorStatus.PAYMENT_AMOUNT_INVALID);
		}
	}
}