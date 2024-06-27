package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.DeliveryReport;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.DeliveryReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DeliveryReportService {
    @Autowired
    DeliveryReportRepository deliveryReportRepository;

    public List<DeliveryReport> getAllDeliveryReports() {
        return deliveryReportRepository.findAll();
    }

    public Integer download_delivery_reports_count(String alias, Date fromDate, Date toDate, Integer customer) {
        String dbTable = getDelReportTable(customer);
        Integer result = null;
        if(Objects.equals(dbTable, "DeliveryReport")){
            result = deliveryReportRepository.download_delivery_reports_count(fromDate,toDate);
        }
        return result;
    }

    public String getDelReportTable(Integer customer) {
        String dB_table = "DeliveryReport";

        if (customer == 42682340) {
            dB_table = "delivery_report_CEB";
        } else if (customer == 999999) {
            dB_table = "delivery_report_Internal";
        } else if (customer == 24794895) {
            dB_table = "delivery_report_Ceylinco";
        } else if (customer == 80808080) {
            dB_table = "delivery_report_mAdv";
        }

        return dB_table;
    }

    public List<Map<String, Object>> download_delivery_reports(String alias, Date fromDate, Date toDate, Integer customer) {
        String dbTable = getDelReportTable(customer);
        List<Map<String, Object>> result = null;
        if(Objects.equals(dbTable, "DeliveryReport")){
            result = deliveryReportRepository.download_delivery_reports(fromDate,toDate,alias);
        }
        return result;
    }
}
