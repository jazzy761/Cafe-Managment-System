package com.inn.cafe.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constant.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<String> generatedReport(Map<String, Object> requestMap) {

        try {
            String filename;
            if (ValidateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    filename = (String) requestMap.get("uuid");
                } else {
                    filename = CafeUtils.getUUID();
                    requestMap.put("uuid", filename);
                    insertBill(requestMap);
                }

                String Data = "Name: " + requestMap.get("name") + "\n" + "ContactNumber: " + requestMap.get("contactNumber") + "\n"
                        + "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod") + "\n";

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + filename + ".pdf"));

                document.open();
                setRectangleInpdf(document);

                Paragraph paragraph = new Paragraph("Cafe Management System\n", getFont("Header"));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                Paragraph line = new Paragraph("_________________________\n\n", getFont("Data"));
                line.setAlignment(Element.ALIGN_CENTER);
                document.add(line);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, CafeUtils.getMapFromString(jsonArray.getString(i)), i);
                }
                document.add(table);

                Paragraph footer = new Paragraph("\nTotal: " + requestMap.get("totalAmount") + "\n\n"
                        + "Thank you for visiting, come again!", getFont("Footer"));
                footer.setAlignment(Element.ALIGN_RIGHT);
                document.add(footer);

                document.close();
                Map<String, String> response = new HashMap<>();
                response.put("uuid", filename);
                //return new ResponseEntity<>(response, HttpStatus.OK);
                return new ResponseEntity<>("{\"uuid\" :\""+filename+"\"}", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("requires data not found", HttpStatus.BAD_REQUEST);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOME_THING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRows(PdfPTable table, Map<String, Object> data, int index) {
        log.info("inside addRows");
        BaseColor rowColor = index % 2 == 0 ? BaseColor.LIGHT_GRAY : BaseColor.WHITE;

        PdfPCell cell = new PdfPCell(new Phrase((String) data.get("name")));
        cell.setBackgroundColor(rowColor);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase((String) data.get("category")));
        cell.setBackgroundColor(rowColor);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase((String) data.get("quantity")));
        cell.setBackgroundColor(rowColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(Double.toString((Double) data.get("price"))));
        cell.setBackgroundColor(rowColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(Double.toString((Double) data.get("total"))));
        cell.setBackgroundColor(rowColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside the addtableheader");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.CYAN);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle, getFont("Header")));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type) {
            case "Header":
                Font headerfont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerfont.setStyle(Font.BOLD);
                return headerfont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                return dataFont;
            case "Footer":
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
                footerFont.setStyle(Font.BOLD);
                return footerFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInpdf(Document document) throws DocumentException {
        log.info("Inside the setRectangleInpdf");
        Rectangle rect = new Rectangle(577, 825, 18, 15);
        rect.enableBorderSide(Rectangle.BOX);
        rect.setBorderWidth(2);
        rect.setBorderColor(BaseColor.GRAY);
        document.add(rect);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactnumber((String) requestMap.get("contactNumber"));
            bill.setPaymentmethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductdetail((String) requestMap.get("productDetails"));
            bill.setCreatedby(jwtFilter.getCurrentUser());
            billDao.save(bill);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean ValidateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }


    @Override
    public ResponseEntity<List<Bill>> getBills() {

        List<Bill> list = new ArrayList<>();
        if(jwtFilter.isAdmin()){
            list = billDao.getAllBills();
        }else{
            list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getpdf(Map<String, Object> requestMap) {
        log.info("Inside get pdf : request pdf{}:" , requestMap);
        try{
            byte[] byteArray = new  byte[0];
            if(!requestMap.containsKey("uuid") && ValidateRequestMap(requestMap)){
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filepath = CafeConstants.STORE_LOCATION+"\\"+(String) requestMap.get("uuid")+ ".pdf";

            if(CafeUtils.isFileExist(filepath)){
                byteArray  = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
            else{
                requestMap.put("isGenerated" , false);
                generatedReport(requestMap);
                byteArray = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    private byte[] getByteArray(String filePath) throws Exception {
        File initialFile = new File(filePath);
        InputStream targetStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            Optional optional = billDao.findById(id);
            if(!optional.isEmpty()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity("Bill Deleted" , HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Bill Not Found" , HttpStatus.NOT_FOUND);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOME_THING_WENT_WRONG , HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
