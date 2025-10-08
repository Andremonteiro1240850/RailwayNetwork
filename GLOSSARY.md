# Railway Logistics Management System - Glossary

## Terms, Expressions and Acronyms (TEA)

| **_TEA_** (EN) | **_TEA_** (PT) | **_Description_** (EN) |
|:---------------|:---------------|:----------------------|
| **Aisle** | **Corredor** | Passageway between rows of storage bays in warehouse for movement and access. |
| **Allocation** | **Alocação** | Process of assigning specific quantities from inventory boxes to fulfill order lines. |
| **Assembly** | **Montagem** | Process of coupling locomotives and wagons to form complete trains. |
| **Audit Log** | **Registo de Auditoria** | File recording all inspection actions and decisions for returned goods. |
| **Bay** | **Área de Armazenamento** | Specific storage location within warehouse identified by coordinates. |
| **Best Fit Decreasing (BFD)** | **Melhor Ajuste Decrescente** | Packing heuristic minimizing remaining unused capacity in trolleys. |
| **Bogies** | **Bogies** | Chassis framework carrying wheelset under railway vehicles for weight distribution. |
| **Box** | **Caixa** | Minimum storage unit containing specific SKU quantity with unique identifier. |
| **Boxcar** | **Vagão Coberto** | Enclosed wagon type for general goods protection. |
| **BoxId** | **Identificador de Caixa** | Unique identifier for each box in warehouse management system. |
| **Bridge** | **Ponte** | Engineering structure crossing obstacles (excluded from project scope). |
| **Capacity** | **Capacidade** | Maximum weight limit for trolleys or storage limit for bays. |
| **Cargo** | **Carga** | Goods or commodities transported by railway including raw materials and products. |
| **Chemical Tank Car** | **Vagão-tanque Químico** | Specialized tank wagon for chemicals with purity specifications. |
| **Circulation Lines** | **Linhas de Circulação** | Terminal tracks for wagon movement between areas. |
| **Complex Route** | **Rota Complexa** | Route that allows transportation of multiple freights together. |
| **Container** | **Contentor** | Standardized cargo unit (20ft, 40ft, 45ft) for intermodal transport. |
| **Cycle Count** | **Contagem Cíclica** | Return reason for inventory audit discrepancies requiring inspection. |
| **Customer Remorse** | **Arrependimento do Cliente** | Return reason where undamaged products can be restocked. |
| **Damaged** | **Danificado** | Return reason for broken products requiring disposal. |
| **Deterministic Sweep** | **Varredura Determinística** | Sequencing strategy sorting locations by ascending aisle number. |
| **Dispatch** | **Expedição** | Operation removing products from storage following FEFO/FIFO rules. |
| **Dispatching** | **Despacho** | Planning train movements to avoid delays and maximize efficiency. |
| **Distance Function (D)** | **Função de Distância (D)** | Formula: D = b1 + \|a1-a2\|*3 + b2 for warehouse movement. |
| **Double Track** | **Via Dupla** | Railway segment with parallel tracks allowing bidirectional movement. |
| **Electrified Line** | **Linha Eletrificada** | Track segment with overhead electrical systems for electric locomotives. |
| **Eligibility Status** | **Estado de Elegibilidade** | Order line classification: ELIGIBLE, PARTIAL, or UNDISPATCHABLE. |
| **Endpoint** | **Ponto Final** | Terminal station where railway lines begin or end. |
| **Expired** | **Expirado** | Return reason for products past expiry date requiring disposal. |
| **Expiry Date** | **Data de Validade** | Date after which perishable products become unsuitable. |
| **FEFO (First-Expired-First-Out)** | **FEFO** | Inventory rule dispatching nearest-expiry products first. |
| **FIFO (First-In-First-Out)** | **FIFO** | Inventory rule dispatching oldest products by receipt date. |
| **First Fit (FF)** | **Primeiro Ajuste** | Packing heuristic placing items in first available trolley. |
| **First Fit Decreasing (FFD)** | **Primeiro Ajuste Decrescente** | Packing heuristic sorting by weight then using first-fit. |
| **Flatcar** | **Vagão Plataforma** | Open wagon for containers, vehicles, and machinery. |
| **Freight** | **Carga** | Goods or commodities being transported by rail. |
| **Freight Car** | **Vagão de Carga** | Railway vehicle designed for transporting goods. |
| **Freight Manager** | **Gestor de Carga** | User profile overseeing cargo operations and planning. |
| **Freight Yard** | **Pátio de Carga** | Railway facility for loading, unloading, and train assembly. |
| **Gauge** | **Bitola** | Distance between inner rail sides affecting vehicle compatibility. |
| **Hopper Car** | **Vagão Graneleiro** | Wagon for bulk materials like coal and grains with bottom discharge. |
| **Infrastructure Owner** | **Proprietário de Infraestrutura** | Entity responsible for railway line maintenance. |
| **Intermodal Transport** | **Transporte Intermodal** | Combining rail with trucks/ships for door-to-door logistics. |
| **Intermodal Terminal** | **Terminal Intermodal** | Facility transferring containers between transport modes. |
| **Inventory Replenishment** | **Reabastecimento de Inventário** | Unloading wagons and storing products following FEFO/FIFO. |
| **Itinerary** | **Itinerário** | The path or sequence of stations a train must pass through. |
| **Line Segment** | **Segmento de Linha** | Individual track section with specific technical characteristics. |
| **Loading Operation** | **Operação de Carregamento** | Process of placing cargo into wagons at terminals. |
| **Locomotive** | **Locomotiva** | Powered rail vehicle providing traction for trains. |
| **Logistical Operator** | **Operador Logístico** | Company managing freight transportation operations. |
| **Main Corridor** | **Corredor Principal** | Central warehouse pathway with entrance at coordinate (0,0). |
| **Mineral Oil Car** | **Vagão de Óleo Mineral** | Tank wagon for petrochemicals and mineral oils. |
| **Nearest Neighbour** | **Vizinho Mais Próximo** | Sequencing strategy selecting closest next location. |
| **Operational Radius** | **Raio Operacional** | Geographic area where station operations influence cargo. |
| **Order** | **Pedido** | Customer request for products with quantities and dates. |
| **Order Line** | **Linha de Pedido** | Individual order item specifying SKU and quantity. |
| **Packaging Heuristic** | **Heurística de Empacotamento** | Algorithm for packing allocations into trolleys. |
| **Parking Lines** | **Linhas de Estacionamento** | Terminal sidings for temporary wagon storage. |
| **Partial Dispatch** | **Expedição Parcial** | Operation continuing to next bay when current bay runs out. |
| **Path** | **Caminho** | Ordered list of stations through which a train must pass. |
| **Perishable Goods** | **Bens Perecíveis** | Products requiring expiry date tracking and FEFO management. |
| **Picking Plan** | **Plano de Picking** | Schedule for product collection from warehouse locations. |
| **Picking Route** | **Rota de Picking** | Optimized path through warehouse minimizing travel distance. |
| **Planner** | **Planeador** | User profile analyzing and adjusting train schedules. |
| **Pressure Gas Car** | **Vagão de Gás sob Pressão** | Tank wagon for liquefied gases requiring pressure. |
| **Quarantine** | **Quarentena** | Controlled area inspecting returned goods before processing. |
| **Railway Infrastructure** | **Infraestrutura Ferroviária** | Physical components including tracks and stations. |
| **Railway Network** | **Rede Ferroviária** | Complete system of interconnected railway lines. |
| **ReceivedAt** | **RecebidoEm** | Timestamp recording box receipt into warehouse system. |
| **Refrigerated Car** | **Vagão Frigorífico** | Temperature-controlled wagon for perishable goods. |
| **Relocation** | **Relocalização** | Operation moving boxes between bays maintaining FEFO position. |
| **Returns** | **Devoluções** | Products sent back requiring inspection and quarantine. |
| **Rolling Stock** | **Material Circulante** | All railway vehicles including locomotives and wagons. |
| **Route** | **Rota** | Ordered station sequence train passes through. |
| **Scheduling** | **Calendarização** | Planning train movements to avoid conflicts and maximize efficiency. |
| **Siding** | **Desvio** | Designated crossing area on single-track segments. |
| **Simple Route** | **Rota Simples** | Route consisting of a single freight transport. |
| **Single Track** | **Via Simples** | Railway segment with one track requiring sidings. |
| **SKU (Stock Keeping Unit)** | **SKU** | Unique code identifying specific product type in inventory. |
| **Sorting** | **Triagem** | Process organizing wagons in freight yards for efficiency. |
| **Stacking** | **Empilhamento** | Container storage method not supported in Europe. |
| **Station** | **Estação** | Nodal point in railway network for logistical operations. |
| **Station Master** | **Chefe de Estação** | User overseeing station operations and coordination. |
| **Station Storage Manager** | **Gestor de Armazenamento** | User responsible for warehouse operations. |
| **Strict Mode** | **Modo Restrito** | Allocation where lines only ELIGIBLE if full quantity allocated. |
| **Tank Car** | **Vagão-tanque** | Wagon for liquids, chemicals, and fuels. |
| **Tare** | **Tara** | Empty wagon weight used in payload calculations. |
| **Terminal** | **Terminal** | Major railway facility for cargo handling and storage. |
| **Train Driver** | **Maquinista** | User operating trains and ensuring safe movement. |
| **Train Operating Company** | **Empresa Operadora** | Entity owning rolling stock and providing services. |
| **Trolley** | **Carrinho** | Picking equipment with specific weight capacity. |
| **Tunnel** | **Túnel** | Underground passage (excluded from project scope). |
| **Unloading Dock** | **Doca de Descarga** | Warehouse access points where wagons are unloaded. |
| **Unloading Operation** | **Operação de Descarga** | Process removing cargo from wagons for storage. |
| **Utilization** | **Utilização** | Trolley capacity metric: usedWeight/capacityWeight. |
| **Validation** | **Validação** | Process checking data integrity during import. |
| **Wagon** | **Vagão** | Rail vehicle for freight transport with type-specific characteristics. |
| **Warehouse** | **Armazém** | Facility organized in aisles and bays for product storage. |
| **Warehousing** | **Armazenagem** | Storage of cargo at freight yards, terminals, and sidings. |
| **Warehouse Layout** | **Layout do Armazém** | Physical organization using coordinate system for distances. |
| **WMS (Warehouse Management System)** | **WMS** | Software tracking all goods movements and locations. |
