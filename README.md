# Excel Data Upload and SMS Campaign Workflow

This workflow describes the process of uploading data from an Excel file to the database and then sending an SMS campaign to a specific range of users.

## Prerequisites

- An Excel file (`.xlsx`) with the following columns in order:
  1. **Id** (Mapped to `excelId`)
  2. **Name**
  3. **Email**
  4. **Phone**
  5. **City**
- A running server on `http://localhost:8080`.

## Workflow Steps

### 1. Upload Excel Data to Database
1. Open the application in your browser (usually `http://localhost:8080`).
2. Drag and drop your `.xlsx` file into the upload area or click to browse.
3. Click the **Upload to Database** button.
4. Wait for the success message: "File uploaded and data stored successfully!"

### 2. Send SMS to a Range of IDs
1. Navigate to the **Send SMS** section below the upload area.
2. Enter the **Start Excel ID** (e.g., `1`).
3. Enter the **End Excel ID** (e.g., `10`).
4. Click the **Send SMS** button.
   - The system will process the records within the numeric range of the IDs provided.
   - An SMS will be sent to each record using the CMT API.
5. Once complete, you will see a message: "SMS sending process completed. Sent to X records."

### 3. Download SMS Logs
1. After the SMS campaign is completed, a **Download Logs** button will appear.
2. Click **Download Logs** to get a CSV file (`sms_logs.csv`) containing:
   - Excel ID
   - Mobile Number
   - Message Content
   - Status (SUCCESS or FAILED)
   - Timestamp
