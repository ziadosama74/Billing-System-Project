# Billing-System-Project
<h2>Billing System Project Flow</h2>
<img width="1472" height="735" alt="image" src="https://github.com/user-attachments/assets/c48ae2f9-0d12-4d3c-9729-2c34ba91d8c8" />
<h2>📊 Billing System Database Design</h2>

## 🏗️ Overview
This database design represents a telecom billing system that processes CDR (Call Detail Records), applies rating based on subscriber plans, and generates invoices.
---
<img width="938" height="1032" alt="ERD" src="https://github.com/user-attachments/assets/acd737d3-6045-4528-8d1c-fa8fe3d99510" />

## 👤 Subscribers

| Column Name                | Data Type | Description            |
| -------------------------- | --------- | ---------------------- |
| SubscriberID (PK)          | INT       | Unique subscriber ID   |
| MSISDN (UNIQUE)            | VARCHAR   | Mobile number          |
| Name                       | VARCHAR   | Subscriber name        |
| InternationalID (UNIQUE)   | VARCHAR   | National ID / Passport |
| Address                    | VARCHAR   | Subscriber address     |
| PlanID (FK → Plans.PlanID) | INT       | Assigned plan          |
| CreatedAt                  | DATETIME  | Creation date          |
| IsDeleted                  | BOOLEAN   | Soft delete flag       |

---

## 📦 Plans

| Column Name | Data Type | Description      |
| ----------- | --------- | ---------------- |
| PlanID (PK) | INT       | Unique plan ID   |
| PlanName    | VARCHAR   | Plan name        |
| Description | VARCHAR   | Plan description |

---

## 💰 Rates

| Column Name                | Data Type | Description        |
| -------------------------- | --------- | ------------------ |
| RateID (PK)                | INT       | Unique rate ID     |
| PlanID (FK → Plans.PlanID) | INT       | Related plan       |
| ServiceType                | VARCHAR   | VOICE / SMS / DATA |
| RatePerUnit                | DECIMAL   | Cost per unit      |
| Unit                       | VARCHAR   | SECOND / SMS / MB  |

---

## 📁 CDR_Files

| Column Name | Data Type | Description                  |
| ----------- | --------- | ---------------------------- |
| FileID (PK) | INT       | Unique file ID               |
| FileName    | VARCHAR   | File name                    |
| ReceivedAt  | DATETIME  | File received time           |
| ProcessedAt | DATETIME  | File processed time          |
| Status      | VARCHAR   | RECEIVED / PROCESSED / ERROR |

---

## 📞 CDRs (Raw Data)

| Column Name                    | Data Type | Description             |
| ------------------------------ | --------- | ----------------------- |
| CDRID (PK)                     | INT       | Unique CDR ID           |
| Caller                         | VARCHAR   | Calling number          |
| Callee                         | VARCHAR   | Called number           |
| StartTime                      | DATETIME  | Call start time         |
| Duration                       | INT       | Duration / usage        |
| ServiceType                    | VARCHAR   | VOICE / SMS / DATA      |
| FileID (FK → CDR_Files.FileID) | INT       | Source file             |
| Status                         | VARCHAR   | NEW / PROCESSED / ERROR |

---

## 🔥 Rated_CDRs

| Column Name                                  | Data Type | Description            |
| -------------------------------------------- | --------- | ---------------------- |
| RatedCDRID (PK)                              | INT       | Unique rated record ID |
| CDRID (FK → CDRs.CDRID)                      | INT       | Original CDR           |
| SubscriberID (FK → Subscribers.SubscriberID) | INT       | Charged subscriber     |
| Cost                                         | DECIMAL   | Calculated cost        |
| RatedAt                                      | DATETIME  | Rating timestamp       |

---

## 🧾 Invoices

| Column Name                                  | Data Type | Description           |
| -------------------------------------------- | --------- | --------------------- |
| InvoiceID (PK)                               | INT       | Unique invoice ID     |
| SubscriberID (FK → Subscribers.SubscriberID) | INT       | Invoice owner         |
| TotalAmount                                  | DECIMAL   | Total billed amount   |
| StartDate                                    | DATE      | Billing period start  |
| EndDate                                      | DATE      | Billing period end    |
| CreatedAt                                    | DATETIME  | Invoice creation time |
| Status                                       | VARCHAR   | GENERATED / PAID      |

---

## 📄 Invoice_Items

| Column Name                         | Data Type | Description     |
| ----------------------------------- | --------- | --------------- |
| ItemID (PK)                         | INT       | Unique item ID  |
| InvoiceID (FK → Invoices.InvoiceID) | INT       | Related invoice |
| CDRID (FK → CDRs.CDRID)             | INT       | Related CDR     |
| Cost                                | DECIMAL   | Item cost       |

---

## 🔗 Relationships

| From Table  | To Table      | Relationship |
| ----------- | ------------- | ------------ |
| Plans       | Subscribers   | 1 : N        |
| Plans       | Rates         | 1 : N        |
| Subscribers | Invoices      | 1 : N        |
| Subscribers | Rated_CDRs    | 1 : N        |
| CDR_Files   | CDRs          | 1 : N        |
| CDRs        | Rated_CDRs    | 1 : 1        |
| Invoices    | Invoice_Items | 1 : N        |
| CDRs        | Invoice_Items | 1 : N        |
---



