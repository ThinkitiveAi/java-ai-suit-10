import React, { useState } from 'react';
import { Box, Typography, IconButton, Stack, Button, Tabs, Tab, Paper, Dialog, DialogTitle, DialogContent, DialogActions, Drawer } from '@mui/material';
import {ArrowBackIosNew,Add,MoreVert,ArrowBackIosNew} from '@mui/icons-material';
import {Switch} from '@mui/material';
import AddClinicianModal from './AddClinicianModal';
import AddStaffModal from './AddStaffModal';
// import ClinicianForm from './ClinicianForm';
// import StaffForm from './StaffForm';

const TOP_TABS = ['Profile', 'Location', 'Users', 'Roles', 'Contacts', 'Print Configuration'];
const STAFF_TABS = ['Staff', 'Clinician'];

const initialClinicians = [
  {
    name: 'Floyd Miles',
    email: 'nathan.roberts@example.com',
    contact: '(270) 555-0117',
    role: 'Clinician',
    npi: '5351022502',
    location: 'Georgetown',
    status: true,
  },
  {
    name: 'Kathryn Murphy',
    email: 'jessica.hanson@example.com',
    contact: '(704) 555-0127',
    role: 'Clinician',
    npi: '9501956750',
    location: 'Georgetown',
    status: true,
  },
  {
    name: 'Bessie Cooper',
    email: 'debbie.baker@example.com',
    contact: '(308) 555-0121',
    role: 'Clinician',
    npi: '7111924081',
    location: 'Georgetown',
    status: true,
  },
  {
    name: 'Marvin McKinney',
    email: 'tanya.hill@example.com',
    contact: '(406) 555-0120',
    role: 'Clinician',
    npi: '6842018413',
    location: 'Georgetown',
    status: false,
  },
];

const SettingsPage: React.FC = () => {
  const [topTab, setTopTab] = useState(2); // Users selected
  const [staffTab, setStaffTab] = useState(1); // Clinician selected
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerType, setDrawerType] = useState<'staff' | 'clinician' | null>(null);

  const [clinicians, setClinicians] = useState(initialClinicians); // your existing clinicians array
  const [staff, setStaff] = useState<any[]>([]); // your staff array

  const handleAddClinician = (clinician: any) => {
    setClinicians(prev => [clinician, ...prev]);
  };

  const handleAddStaff = (staffUser: any) => {
    setStaff(prev => [staffUser, ...prev]);
  };

  return (
    <Box sx={{ background: '#F5F6F8', minHeight: '100vh', p: { xs: 1, md: 3 }, fontFamily: 'Figtree' }}>
      {/* Top bar */}
      <Stack direction="row" alignItems="center" spacing={1} mb={2}>
        <IconButton size="small" sx={{ color: '#373D41' }}>
          <ArrowBackIosNew fontSize="small" />
        </IconButton>
        <Typography sx={{ fontWeight: 500, fontSize: 16, color: '#0E151D' }}>Settings</Typography>
      </Stack>
      {/* Horizontal tabs */}
      <Paper elevation={0} sx={{ background: '#EFF0F2', borderRadius: 2, p: 0, mb: 3, border: '1px solid #F5F5F5' }}>
        <Tabs
          value={topTab}
          onChange={(_, v) => setTopTab(v)}
          variant="scrollable"
          scrollButtons={false}
          sx={{
            minHeight: 0,
            '& .MuiTab-root': {
              minHeight: 0,
              minWidth: 120,
              fontFamily: 'Figtree',
              fontWeight: 400,
              fontSize: 14,
              color: '#373D41',
              borderRadius: 1.5,
              px: 2,
              py: 1.5,
              textTransform: 'none',
              background: '#EFF0F2',
              m: 0.5,
            },
            '& .Mui-selected': {
              background: '#fff',
              color: '#004FD9',
              fontWeight: 500,
              boxShadow: '0px 1px 2px 0px rgba(16, 24, 40, 0.06), 0px 1px 3px 0px rgba(16, 23, 40, 0.1)',
            },
          }}
        >
          {TOP_TABS.map((tab, i) => (
            <Tab key={tab} label={tab} />
          ))}
        </Tabs>
      </Paper>
      {/* Staff/Clinician tabs and Add Clinician button */}
      <Stack direction="row" alignItems="center" justifyContent="space-between" mb={2}>
        <Stack direction="row" spacing={0}>
          {STAFF_TABS.map((tab, i) => (
            <Button
              key={tab}
              onClick={() => setStaffTab(i)}
              sx={{
                borderRadius: '12px 12px 0 0',
                background: staffTab === i ? '#EEF7FE' : 'transparent',
                color: staffTab === i ? '#004FD9' : '#595F63',
                fontFamily: 'Figtree',
                fontWeight: staffTab === i ? 500 : 400,
                fontSize: 14,
                px: 3,
                py: 1.5,
                minWidth: 120,
                boxShadow: 'none',
                borderBottom: staffTab === i ? '2px solid #004FD9' : '2px solid #E7E7E7',
                textTransform: 'none',
                height: 43,
                '&:hover': {
                  background: '#EEF7FE',
                },
              }}
            >
              {tab}
            </Button>
          ))}
        </Stack>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            setDrawerType(staffTab === 0 ? 'staff' : 'clinician');
            setDrawerOpen(true);
          }}
          sx={{
            background: '#0068FF',
            color: '#fff',
            borderRadius: 1,
            fontFamily: 'Figtree',
            fontWeight: 500,
            fontSize: 14,
            px: 2.5,
            py: 1,
            boxShadow: '0px 1px 2px 0px rgba(16, 24, 40, 0.05)',
            textTransform: 'none',
            '&:hover': { background: '#0052CC' },
          }}
        >
          {staffTab === 0 ? 'Add Staff' : 'Add Clinician'}
        </Button>
      </Stack>
      {/* Table */}
      <Paper elevation={0} sx={{ borderRadius: 2, overflow: 'hidden', border: '1px solid #E7E7E7' }}>
        <Box sx={{ display: 'flex', background: '#F5F5F5', p: 2 }}>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Name</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Email ID</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Contact Number</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Role</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>NPI Number</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Work Location</Typography>
          <Typography sx={{ flex: 1, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Status</Typography>
          <Typography sx={{ width: 40, fontWeight: 600, fontSize: 14, color: '#74797B' }}>Action</Typography>
        </Box>
        {clinicians.map((row, idx) => (
          <Box key={row.email} sx={{ display: 'flex', alignItems: 'center', borderTop: idx === 0 ? 'none' : '1px solid #E7E7E7', p: 2 }}>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.name}</Typography>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.email}</Typography>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.contact}</Typography>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.role}</Typography>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.npi}</Typography>
            <Typography sx={{ flex: 1, color: '#21262B', fontWeight: 400, fontSize: 14 }}>{row.location}</Typography>
            <Box sx={{ flex: 1 }}>
              <Switch checked={row.status} color="success" />
            </Box>
            <Box sx={{ width: 40 }}>
              <IconButton>
                <MoreVert sx={{ color: '#373D41' }} />
              </IconButton>
            </Box>
          </Box>
        ))}
      </Paper>
      {/* Add Clinician Modal */}
      <Drawer
        anchor="right"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        PaperProps={{ sx: { width: { xs: '100vw', sm: 400 } } }}
      >
        {drawerType === 'clinician' && (
          <ClinicianForm
            onSubmit={(clinician) => {
              handleAddClinician(clinician);
              setDrawerOpen(false);
            }}
            onCancel={() => setDrawerOpen(false)}
          />
        )}
        {drawerType === 'staff' ? (
          <AddStaffModal
            open={true}
            onClose={() => setDrawerOpen(false)}
            onAdd={(staffUser) => {
              handleAddStaff(staffUser);
              setDrawerOpen(false);
            }}
          />
        ) : null}
      </Drawer>
    </Box>
  );
};

export default SettingsPage; 