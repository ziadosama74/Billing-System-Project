# 🚀 Billing-System-Project

---

## 🔄 Billing System Project Flow

<p align="center">
  <img width="90%" src="https://github.com/user-attachments/assets/c48ae2f9-0d12-4d3c-9729-2c34ba91d8c8" />
</p>

---

## 📊 Billing System Database Design

## 🏗️ Overview
This database design represents a telecom billing system that processes CDR (Call Detail Records), applies rating based on subscriber plans, and generates invoices.

<p align="center">
  <img width="60%" src="https://github.com/user-attachments/assets/acd737d3-6045-4528-8d1c-fa8fe3d99510" />
</p>

---

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

## ✨ Project Features

### 📂 CDR Parsing Engine
- 📥 Automatically detects and loads incoming **CDR CSV files**
- 🔍 Parses call records (Caller, Called, Duration, Service Type, etc.)
- ⚙️ Processes records efficiently with validation
- 📦 Moves processed files to backup directory

---

### 💰 Rating & Charging Engine
- 📡 Receives trigger from parsing process
- ⚙️💰 Calculates charges based on service type (VOICE, SMS, DATA)
- 📊 Prepares records for billing
- ✅ Ensures accurate and consistent rating

---

### 🔗 Real-Time Communication
- 🌐 Socket-based communication between **Parsing** and **Rating**
- 📤 Sends `START_RATING` signal after file processing
- 📥 Waits for `RATING_DONE` acknowledgment
- 🔄 Ensures synchronized workflow between components

---

### 🗄️ Database Integration
- 🧩 Uses **PostgreSQL** for storing CDR data
- ⚡ Optimized insertion using stored procedures
- 🔐 Ensures data consistency and integrity

---

### 🖥️ System Automation
- 🔁 Continuously monitors directory for new files
- ⏳ Handles empty states gracefully
- 🚀 Fully automated pipeline (Parsing → Rating → Backup)

<p align="center">
  <img width="85%" src="https://github.com/user-attachments/assets/ba7261c5-da22-4816-a601-728d26875bcc" />
</p>

---

### 🎛️ Clean Logging System
- 📊 Structured logs with levels: `[PARSING]`, `[RATING]`, `[NETWORK]`
- 🎨 Colored console output for better readability
- 🧠 Easy debugging and monitoring

---

### ⚡ Scalable Architecture
- 🧱 Modular design (Parsing, Rating, Network layers)
- 🔌 Easy to extend (Billing, Invoicing, Web UI)
- 🚀 Ready for real-world telecom system simulation

---
