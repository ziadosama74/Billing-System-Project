# 🚀 Billing-System-Project

---

## 🔄 Billing System Project Flow

<p align="center">
  <img width="90%" src="https://github.com/user-attachments/assets/c48ae2f9-0d12-4d3c-9729-2c34ba91d8c8" />
</p>

<p align="center">
  <img width="90%" alt="Flow" src="https://github.com/user-attachments/assets/da2ac8e3-ae90-4a7a-b917-a595db69fc2e" />
</p>

---

# 📡 Postpaid Billing System

## 📌 Overview

This project implements a **Postpaid Billing System** similar to real telecom operators.
The system processes customer usage (Calls, SMS, Data), applies subscription plans, and generates monthly invoices based on consumption.

---

## ⚙️ How the System Works

The system follows a complete billing pipeline:

```
CDRs → Rating → Usage Tracking → Billing → Invoicing
```

---

## 🧾 1. Subscription Plans

Each subscriber is assigned a plan that includes:

* Monthly Fee (fixed cost)
* Included Units (main bundle)
* Free Units (bonus usage)

### Example Plans

| Plan     | Monthly Fee | Description         |
| -------- | ----------- | ------------------- |
| Basic    | 75 EGP      | Light users         |
| Standard | 150 EGP     | Regular users       |
| Premium  | 300 EGP     | Heavy users         |
| Business | 600 EGP     | Enterprise solution |

---

## 🎁 2. Bundle Structure

Each plan provides usage limits per service:

| Service | Main Units | Free Units    |
| ------- | ---------- | ------------- |
| VOICE   | Minutes    | Bonus Minutes |
| SMS     | Messages   | Bonus SMS     |
| DATA    | MB         | Bonus Data    |

### Consumption Order

```
Main Bundle → Free Units → Charged Usage
```

---

## 📊 3. Usage Processing (CDRs)

All user activities are stored as **Call Detail Records (CDRs)**:

* VOICE → Call duration
* SMS → Number of messages
* DATA → Internet usage (MB)

---

## 💰 4. Rating Engine

The **Rating Engine** calculates the cost of each CDR.

### Steps:

1. Identify subscriber plan
2. Convert usage units (minutes, SMS, MB)
3. Deduct from available bundle
4. Charge only exceeded usage

### Pricing Model

| Service | Rate           |
| ------- | -------------- |
| VOICE   | 0.30 EGP / MIN |
| SMS     | 0.20 EGP / SMS |
| DATA    | 0.02 EGP / MB  |

---

## 🧮 5. Cost Calculation

Two types of costs are calculated:

* **Actual Cost** → Total usage without bundle
* **Charged Cost** → Only exceeded usage after bundle consumption

---

## 🧾 6. Billing & Invoicing

At the end of the billing cycle:

```
Final Bill = Monthly Fee + Over Usage
```

### Example

* Plan: Standard (150 EGP)
* Usage exceeds bundle: 50 minutes

```
Extra Cost = 50 × 0.30 = 15 EGP
Final Bill = 165 EGP
```

---

## 🗄️ 7. Database Design

### 📊 ERD

<p align="center">
  <img width="90%" alt="ERD" src="https://github.com/user-attachments/assets/23c3428d-298b-41e9-a0e0-b3e00336dc7e" />
</p>

---

## 🧾 Subscribers

| Column          | Type      | Description           |
| --------------- | --------- | --------------------- |
| subscriberid    | INT (PK)  | Unique subscriber ID  |
| msisdn          | VARCHAR   | Phone number (Unique) |
| name            | VARCHAR   | Subscriber name       |
| internationalid | VARCHAR   | National ID           |
| address         | VARCHAR   | Address               |
| createdat       | TIMESTAMP | Creation date         |
| isdeleted       | BOOLEAN   | Soft delete flag      |
| planid          | INT (FK)  | Reference to Plans    |

---

## 📦 Plans

| Column      | Type     | Description              |
| ----------- | -------- | ------------------------ |
| planid      | INT (PK) | Plan ID                  |
| planname    | VARCHAR  | Plan name                |
| monthlyfee  | DECIMAL  | Monthly subscription fee |
| description | VARCHAR  | Plan description         |
| isactive    | BOOLEAN  | Active status            |

---

## 🎁 Plan Allowances (Main Units)

| Column        | Type     | Description        |
| ------------- | -------- | ------------------ |
| allowanceid   | INT (PK) | Allowance ID       |
| planid        | INT (FK) | Reference to Plans |
| servicetype   | VARCHAR  | VOICE / SMS / DATA |
| includedunits | INT      | Included units     |

---

## 🎁 Plan Free Units

| Column      | Type     | Description        |
| ----------- | -------- | ------------------ |
| freeunitid  | INT (PK) | Free unit ID       |
| planid      | INT (FK) | Reference to Plans |
| servicetype | VARCHAR  | VOICE / SMS / DATA |
| freeunits   | INT      | Free units         |

---

## 💰 Rates

| Column      | Type     | Description    |
| ----------- | -------- | -------------- |
| rateid      | INT (PK) | Rate ID        |
| servicetype | VARCHAR  | Service type   |
| rateperunit | DECIMAL  | Cost per unit  |
| unit        | VARCHAR  | MIN / SMS / MB |

---

## 📁 CDR Files

| Column      | Type      | Description    |
| ----------- | --------- | -------------- |
| fileid      | INT (PK)  | File ID        |
| filename    | VARCHAR   | File name      |
| receivedat  | TIMESTAMP | Received time  |
| processedat | TIMESTAMP | Processed time |
| status      | VARCHAR   | File status    |

---

## 📞 CDRs

| Column      | Type      | Description            |
| ----------- | --------- | ---------------------- |
| cdrid       | INT (PK)  | CDR ID                 |
| caller      | VARCHAR   | Caller number          |
| called      | VARCHAR   | Called number          |
| starttime   | TIMESTAMP | Call start             |
| duration    | INT       | Duration / Units       |
| servicetype | VARCHAR   | VOICE / SMS / DATA     |
| fileid      | INT (FK)  | Reference to CDR Files |
| status      | VARCHAR   | CDR status             |

---

## 📊 Subscriber Usage

| Column       | Type     | Description   |
| ------------ | -------- | ------------- |
| usageid      | INT (PK) | Usage ID      |
| subscriberid | INT (FK) | Subscriber    |
| servicetype  | VARCHAR  | Service type  |
| usedunits    | INT      | Used units    |
| billingcycle | DATE     | Billing month |

---

## 📈 Rated CDRs

| Column       | Type      | Description        |
| ------------ | --------- | ------------------ |
| ratedcdrid   | INT (PK)  | Rated CDR ID       |
| cdrid        | INT (FK)  | Reference to CDR   |
| subscriberid | INT (FK)  | Subscriber         |
| actual_cost  | DECIMAL   | Cost before bundle |
| charged_cost | DECIMAL   | Cost after bundle  |
| ratedat      | TIMESTAMP | Rating time        |

---

## 🧾 Invoices

| Column       | Type      | Description    |
| ------------ | --------- | -------------- |
| invoiceid    | INT (PK)  | Invoice ID     |
| subscriberid | INT (FK)  | Subscriber     |
| totalamount  | DECIMAL   | Final bill     |
| startdate    | DATE      | Billing start  |
| enddate      | DATE      | Billing end    |
| createdat    | TIMESTAMP | Created time   |
| status       | VARCHAR   | Invoice status |

---

## 🧾 Invoice Items

| Column    | Type     | Description  |
| --------- | -------- | ------------ |
| itemid    | INT (PK) | Item ID      |
| invoiceid | INT (FK) | Invoice      |
| cdrid     | INT (FK) | CDR          |
| cost      | DECIMAL  | Charged cost |

---

# 🔗 Relationships Summary

* Subscribers → Plans (Many-to-One)
* Plans → Allowances / Free Units (One-to-Many)
* CDR Files → CDRs (One-to-Many)
* Subscribers → Usage (One-to-Many)
* CDRs → Rated CDRs (One-to-One)
* Subscribers → Invoices (One-to-Many)
* Invoices → Invoice Items (One-to-Many)

---

# ⚙️ System Components

---

## 📂 CDR Parsing Engine

* Automatically detects and loads incoming **CDR CSV files**
* Parses call records (Caller, Called, Duration, Service Type, etc.)
* Processes records with validation
* Moves processed files to backup directory

---

## 💰 Rating & Charging Engine

* Receives trigger from parsing process
* Calculates charges based on service type (VOICE, SMS, DATA)
* Applies bundle logic (Main + Free Units)
* Stores results in database

---

## 🔗 Real-Time Communication

* Socket-based communication between **Parsing** and **Rating**
* Sends `START_RATING` signal after file processing
* Waits for `RATING_DONE` acknowledgment
* Ensures synchronized workflow

---

## 🗄️ Database Integration

* Uses **PostgreSQL**
* Optimized stored procedures
* Ensures consistency and integrity

---

## 🔁 System Automation

* Continuously monitors directory for new files
* Handles empty states gracefully
* Fully automated pipeline

---

## 🎛️ Logging System

* Structured logs:

  * `[PARSING]`
  * `[RATING]`
  * `[NETWORK]`
* Colored console output
* Easy debugging and monitoring

---

## ⚡ Scalable Architecture

* Modular design (Parsing, Rating, Network layers)
* Easy to extend (Billing, Invoicing, Web UI)
* Ready for real-world telecom simulation

---

## 🚀 Key Features

* ✔ Full Postpaid Billing Logic
* ✔ Multi-service support (VOICE, SMS, DATA)
* ✔ Bundle + Free Units handling
* ✔ Accurate over-usage charging
* ✔ Monthly invoice generation
* ✔ Scalable database design

---
