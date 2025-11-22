-- Create bonds table
CREATE TABLE bonds (
                       id VARCHAR(255) PRIMARY KEY,
                       bond_id VARCHAR(255) NOT NULL UNIQUE,
                       project_name VARCHAR(255) NOT NULL,
                       project_description TEXT NOT NULL,
                       bond_type VARCHAR(50) NOT NULL,
                       status VARCHAR(50) NOT NULL,
                       total_supply NUMERIC(19,4) NOT NULL,
                       face_value NUMERIC(19,4) NOT NULL,
                       coupon_rate NUMERIC(8,4) NOT NULL,
                       maturity_date DATE NOT NULL,
                       issue_date DATE NOT NULL,
                       issuer_id VARCHAR(255) NOT NULL,
                       issuer_name VARCHAR(255) NOT NULL,
                       project_wallet_address VARCHAR(255) NOT NULL,
                       verifier_report_hash VARCHAR(255),
                       blockchain_tx_hash VARCHAR(255),
                       bond_contract_address VARCHAR(255),
                       esg_standard VARCHAR(100),
                       greenium_details VARCHAR(500),
                       use_of_proceeds TEXT,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       version BIGINT NOT NULL DEFAULT 0
);

-- Create bond_allocations table
CREATE TABLE bond_allocations (
                                  id VARCHAR(255) PRIMARY KEY,
                                  bond_id VARCHAR(255) NOT NULL,
                                  investor_id VARCHAR(255) NOT NULL,
                                  investor_name VARCHAR(255) NOT NULL,
                                  allocated_amount NUMERIC(19,4) NOT NULL,
                                  allocated_units NUMERIC(19,4) NOT NULL,
                                  allocation_status VARCHAR(50) NOT NULL,
                                  blockchain_token_id VARCHAR(255),
                                  allocated_at TIMESTAMP NOT NULL,
                                  settled_at TIMESTAMP,
                                  FOREIGN KEY (bond_id) REFERENCES bonds(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_bonds_issuer_id ON bonds(issuer_id);
CREATE INDEX idx_bonds_status ON bonds(status);
CREATE INDEX idx_bonds_maturity_date ON bonds(maturity_date);
CREATE INDEX idx_bond_allocations_bond_id ON bond_allocations(bond_id);
CREATE INDEX idx_bond_allocations_investor_id ON bond_allocations(investor_id);
CREATE UNIQUE INDEX idx_bond_allocations_bond_investor ON bond_allocations(bond_id, investor_id);